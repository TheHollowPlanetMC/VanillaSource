use once_cell::unsync::Lazy;
use crate::minecraft;
use crate::minecraft::blocks;
use crate::minecraft::blocks::NoiseDirection::{NONE, XYZ};
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
    block_id: i32,
    collision_shape: NoisedShape,
    shape: NoisedShape
}

pub struct NoisedShape {
    pub direction: NoiseDirection,
    pub based_shape: VoxelShape
}

static EMPTY_SHAPE: VoxelShape = VoxelShape {
    aabb_list: vec![]
};

impl NoisedShape {
    pub fn get_shape(&self, block_x: i32, block_y: i32, block_z: i32) -> VoxelShape {
        let direction: i32 = self.direction.clone() as i32;

        return if direction == NoiseDirection::NONE as i32 {
            self.based_shape.clone()
        } else {
            let noise: i64 = minecraft::math::create_plant_noise(block_x, block_y, block_z);

            let x = (((noise & 15_i64) as f32 / 15.0_f32) as f64 - 0.5_f64) * 0.5_f64;
            let mut y = 0.0_f64;
            if direction == NoiseDirection::XYZ as i32 {
                y = (((noise >> 4 & 15_i64) as f32 / 15.0_f32) as f64 - 1.0_f64) * 0.2_f64;
            }
            let z = (((noise >> 8 & 15_i64) as f32 / 15.0_f32) as f64 - 0.5_f64) * 0.5_f64;

            let new_shape: VoxelShape = self.based_shape.shift(x, y, z);
            new_shape
        }
    }
}

#[derive(Copy, Clone)]
pub enum NoiseDirection {
    NONE = 0,
    XZ = 1,
    XYZ = 2
}

impl BlockData{
    pub fn get_id(&self) -> i32 {
        return self.block_id;
    }

    pub fn get_collision_shape(&self) -> &NoisedShape {
        return &self.collision_shape;
    }

    pub fn get_shape(&self) -> &NoisedShape {
        return &self.shape;
    }
}

pub fn register_block_in_order(id: i32, collision_shape: NoisedShape, shape: NoisedShape){
    let temp = id;
    let data = BlockData {
        block_id: temp,
        collision_shape,
        shape
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