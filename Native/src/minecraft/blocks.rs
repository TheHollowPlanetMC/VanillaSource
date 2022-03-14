use std::collections::HashMap;
use std::sync::RwLock;
use once_cell::sync::Lazy;
use crate::minecraft::bounding_box::VoxelShape;

//Static block registry
static mut BLOCK_DATA_REGISTRY: Lazy<BlockRegistry> = Lazy::new(BlockRegistry::new);

//Registry struct
struct BlockRegistry{
    data_storage: Vec<BlockData>,
}

impl BlockRegistry {
    fn new() -> Self {
        let mut data_storage: Vec<BlockData> = Vec::new();
        Self{
            data_storage,
        }
    }

    fn set_block_data(&mut self, id: i32, block_data: BlockData){
        self.data_storage.insert(id as usize, block_data);
    }

    fn add_block_data(&mut self, block_data: BlockData){
        self.data_storage.push(block_data);
    }

    fn get_block_data(&self, id: i32) -> &BlockData{
        return &self.data_storage[id as usize];
    }

}

pub struct BlockData {
    pub block_id: i32,
    pub collision_shape: VoxelShape
}

pub fn register_block_in_order(id: i32, collision_shape: VoxelShape){
    let temp = id;
    let data = BlockData {
        block_id: temp,
        collision_shape
    };

    unsafe {
        BLOCK_DATA_REGISTRY.add_block_data(data);
    }
}

pub fn get_block_data_from_id(id: i32) -> &'static BlockData{
    unsafe {
        let data: &BlockData = BLOCK_DATA_REGISTRY.get_block_data(id);
        return data;
    }
}