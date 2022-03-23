use std::collections::HashMap;
use std::rc::Rc;
use std::cell::RefCell;
use std::sync::{Arc, RwLock};
use once_cell::sync::Lazy;
use crate::{BlockData, minecraft};
use crate::collision::collisions::CollideOption;

//World caches
static mut GLOBAL_WORLD_REGISTRY: Lazy<GlobalWorldRegistry> = Lazy::new(GlobalWorldRegistry::new);

thread_local!(pub static THREAD_LOCAL_WORLD_REGISTRY: RefCell<LocalWorldRegistry> = {
    let mut registry = LocalWorldRegistry::new();
    RefCell::new(registry)
});

pub struct GlobalWorldRegistry {
    world_map: HashMap<Vec<u16>, GlobalWorld>
}

impl GlobalWorldRegistry {
    fn new() -> Self {
        let mut world_map: HashMap<Vec<u16>, GlobalWorld> = HashMap::new();
        Self {
            world_map
        }
    }

    pub fn get_world(&mut self, world_name: Vec<u16>) -> &mut GlobalWorld {
        let map = &mut self.world_map;
        if !map.contains_key(&world_name) {
            let mut world: GlobalWorld = GlobalWorld::new(world_name.clone());

            map.insert(world_name.clone(), world);
        }

        return map.get_mut(&world_name).unwrap();
    }
}

pub struct LocalWorldRegistry {
    world_map: HashMap<Vec<u16>, Rc<RefCell<LocalWorld>>>
}

impl LocalWorldRegistry {
    fn new() -> Self {
        let mut world_map: HashMap<Vec<u16>, Rc<RefCell<LocalWorld>>> = HashMap::new();
        Self {
            world_map
        }
    }

    pub fn get_world(&mut self, world_name: Vec<u16>) -> Rc<RefCell<LocalWorld>> {
        let map = &mut self.world_map;
        if !map.contains_key(&world_name) {
            let mut world: LocalWorld = LocalWorld::new(world_name.clone());

            map.insert(world_name.clone(), Rc::new(RefCell::new(world)));
        }

        return map.get_mut(&world_name).unwrap().clone();
    }
}

pub fn get_global_world(world_name: Vec<u16>) -> &'static mut GlobalWorld {
    unsafe {
        return GLOBAL_WORLD_REGISTRY.get_world(world_name);
    }
}

pub fn get_local_world(world_name: Vec<u16>) -> Rc<RefCell<LocalWorld>> {
    THREAD_LOCAL_WORLD_REGISTRY.with(|map| {
        let mut world = map.borrow_mut();
        return world.get_world(world_name);
    })
}


pub struct GlobalWorld {
    pub world_name: Vec<u16>,
    pub chunk_map: RwLock<HashMap<i64, Arc<Chunk>>>
}

impl GlobalWorld {

    fn new(world_name: Vec<u16>) -> Self {
        let mut map: HashMap<i64, Arc<Chunk>> = HashMap::new();
        let mut chunk_map = RwLock::new(map);
        Self {
            world_name,
            chunk_map
        }
    }

    pub fn set_chunk_for_single_thread(&mut self, chunk: Chunk) {
        let key = minecraft::math::get_chunk_key(chunk.chunk_x, chunk.chunk_z);
        self.chunk_map.get_mut().unwrap().insert(key, Arc::new(chunk));
    }

    pub fn get_chunk_at(&self, chunk_x: i32, chunk_z: i32) -> Option<Arc<Chunk>> {
        let key = minecraft::math::get_chunk_key(chunk_x, chunk_z);
        return self.chunk_map.read().unwrap().get(&key).cloned();
    }

    pub fn get_block_data(&self, block_x: i32, block_y: i32, block_z: i32) -> Option<&'static BlockData> {
        let chunk = self.get_chunk_at(block_x >> 4, block_z >> 4);
        return if chunk.is_some() {
            chunk.unwrap().get_block_data(block_x, block_y, block_z)
        } else {
            Option::None
        }
    }

}



pub struct LocalWorld {
    pub world_name: Vec<u16>,
    pub chunk_map: HashMap<i64, Arc<Chunk>>,
    pub global_world: &'static GlobalWorld
}

impl LocalWorld {

    pub(crate) fn new(world_name: Vec<u16>) -> Self {
        let chunk_map: HashMap<i64, Arc<Chunk>> = HashMap::new();
        let global_world = get_global_world(world_name.clone());
        Self {
            world_name,
            chunk_map,
            global_world
        }
    }

    pub fn get_chunk_at(&mut self, chunk_x: i32, chunk_z: i32) -> Option<Arc<Chunk>> {
        let key = minecraft::math::get_chunk_key(chunk_x, chunk_z);
        let option = self.chunk_map.get(&key);
        if option.is_some() {
            option.cloned()
        } else {
            let global = self.global_world.get_chunk_at(chunk_x, chunk_z);
            if global.is_some() {
                self.chunk_map.insert(key, global.as_ref().unwrap().clone());
            }

            return global;
        }
    }

    pub fn get_block_data(&mut self, block_x: i32, block_y: i32, block_z: i32) -> Option<&'static BlockData> {
        let chunk = self.get_chunk_at(block_x >> 4, block_z >> 4);
        return if chunk.is_some() {
            chunk.unwrap().get_block_data(block_x, block_y, block_z)
        } else {
            Option::None
        }
    }

}




pub struct Chunk {
    pub chunk_x: i32,
    pub chunk_z: i32,
    pub sections: Vec<Option<ChunkSection>>
}

impl Chunk {
    pub fn new(chunk_x: i32, chunk_z: i32, sections: Vec<Option<ChunkSection>>) -> Self {
        Self {
            chunk_x,
            chunk_z,
            sections
        }
    }

    pub fn set_section_for_single_thread(&mut self, index: usize, section: ChunkSection){
        self.sections[index] = Option::Some(section);
    }

    pub fn get_block_data(&self, block_x: i32, block_y: i32, block_z: i32) -> Option<&'static BlockData> {
        let index = minecraft::chunk_util::get_section_index(block_y);
        let option = &self.sections[index];
        return if option.is_some() {
            option.as_ref().unwrap().get_block_data(block_x, block_y, block_z)
        } else {
            Option::None
        }
    }


    pub fn has_collision(&self, block_x: i32, block_y: i32, block_z: i32, collide_option: &CollideOption) -> bool {
        let index = minecraft::chunk_util::get_section_index(block_y);
        let option = &self.sections[index];
        return if option.is_none() {
            false
        } else {
            let option = option.as_ref().unwrap().get_block_data(block_x, block_y, block_z);
            if option.is_none() {
                return false;
            }

            let block_data = option.unwrap();

            if collide_option.is_ignore_passable_block {
                let collision = block_data.get_collision_shape().get_shape(0, 0, 0);
                !collision.is_empty()
            } else {
                let collision = block_data.get_shape().get_shape(0, 0, 0);
                !collision.is_empty()
            }
        }
    }

}




pub struct ChunkSection {
    block_pallet: Vec<Option<&'static BlockData>>
}

impl ChunkSection {
    pub fn new(block_pallet: Vec<Option<&'static BlockData>>) -> Self {
        Self{
            block_pallet
        }
    }

    pub fn get_block_data(&self, block_x: i32, block_y: i32, block_z: i32) -> Option<&'static BlockData> {
        let index = minecraft::math::get_yzx_index(block_x & 0xF, block_y & 0xF, block_z & 0xF);
        return self.block_pallet[index as usize];
    }
}
