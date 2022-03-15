use std::collections::HashMap;
use std::sync::RwLock;
use once_cell::sync::Lazy;
use crate::{BlockData, minecraft};

//World caches
static mut GLOBAL_WORLD_REGISTRY: Lazy<AsyncWorldRegistry> = Lazy::new(AsyncWorldRegistry::new);

pub struct AsyncWorldRegistry {
    world_map: HashMap<Vec<u16>, AsyncWorld>
}

impl AsyncWorldRegistry {
    fn new() -> Self {
        let mut world_map: HashMap<Vec<u16>, AsyncWorld> = HashMap::new();
        Self {
            world_map
        }
    }

    pub fn get_world(&mut self, world_name: Vec<u16>) -> &mut AsyncWorld {
        let map = &mut self.world_map;
        if !map.contains_key(&world_name) {
            let mut world: AsyncWorld = AsyncWorld::new(world_name.clone());

            map.insert(world_name.clone(), world);
        }

        return map.get_mut(&world_name).unwrap();
    }
}

pub fn get_world(world_name: Vec<u16>) -> &'static mut AsyncWorld {
    unsafe {
        return GLOBAL_WORLD_REGISTRY.get_world(world_name);
    }
}


pub struct AsyncWorld {
    pub world_name: Vec<u16>,
    pub chunk_map: RwLock<HashMap<i64, Chunk>>
}

impl AsyncWorld {

    fn new(world_name: Vec<u16>) -> Self {
        let mut map: HashMap<i64, Chunk> = HashMap::new();
        let mut chunk_map = RwLock::new(map);
        Self {
            world_name,
            chunk_map
        }
    }

    pub fn set_chunk_for_single_thread(&mut self, chunk: Chunk) {
        let key = minecraft::math::get_chunk_key(chunk.chunk_x, chunk.chunk_z);
        self.chunk_map.get_mut().unwrap().insert(key, chunk);
    }

    pub fn get_chunk_at(&mut self, chunk_x: i32, chunk_z: i32) -> Option<&mut Chunk> {
        let key = minecraft::math::get_chunk_key(chunk_x, chunk_z);
        return (&mut self.chunk_map).get_mut().unwrap().get_mut(&key);
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


pub fn test() {
    let mut world: &mut AsyncWorld = &mut AsyncWorld {
        world_name: vec![0],
        chunk_map: Default::default()
    };
    unsafe {
        world = GLOBAL_WORLD_REGISTRY.get_world(vec![0]);
    }

    let chunk = world.get_chunk_at(0, 0).unwrap();

    let section: ChunkSection = ChunkSection {
        block_pallet: vec![Option::Some(minecraft::blocks::get_block_data_from_id(0)); 4096]
    };

    chunk.set_section_for_single_thread(0, section);
}