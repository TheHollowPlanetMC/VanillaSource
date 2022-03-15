mod minecraft;

extern crate jni;

use jni::sys::{jboolean, jchar, jcharArray, jclass, jdouble, jdoubleArray, jint, jintArray, jsize, jstring};
use jni::JNIEnv;
use jni::*;
use crate::minecraft::blocks::{BlockData, NoiseDirection, NoisedShape, register_block_in_order};
use crate::minecraft::bounding_box::{AABB, VoxelShape};
use crate::minecraft::worlds::{Chunk, ChunkSection};
use crate::NoiseDirection::{NONE, XYZ, XZ};

//Library version
const VERSION: jint = 0;

static mut IS_HIGHER_V1_18_R1: bool = true;

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_getLibraryVersion(env: JNIEnv, class: jclass) -> jint {
    return VERSION;
}

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_registerBlockInOrder(env: JNIEnv, class: jclass, id: jint, collision: jdoubleArray, shape: jdoubleArray, noised_direction: jint){
    let length: jsize = env.get_array_length(collision).unwrap();

    let mut arrayBuffer: Vec<jdouble> = vec![0.0; length as usize];
    let arraySlice = &mut arrayBuffer;

    env.get_double_array_region(collision, 0, arraySlice);

    static EMPTY_AABB: AABB = AABB{
        min_x: 0.0,
        min_y: 0.0,
        min_z: 0.0,
        max_x: 0.0,
        max_y: 0.0,
        max_z: 0.0
    };

    let aabbs: jsize = length / 6;
    let mut collision_aabb_list: Vec<AABB> = vec![EMPTY_AABB.clone(); aabbs as usize];
    for i in 0..aabbs {
        let min_index = (i * 6) as usize;
        collision_aabb_list[i as usize] = AABB{
            min_x: arraySlice[min_index],
            min_y: arraySlice[min_index + 1],
            min_z: arraySlice[min_index + 2],
            max_x: arraySlice[min_index + 3],
            max_y: arraySlice[min_index + 4],
            max_z: arraySlice[min_index + 5]
        };
    }

    let length: jsize = env.get_array_length(collision).unwrap();

    let mut arrayBuffer: Vec<jdouble> = vec![0.0; length as usize];
    let arraySlice = &mut arrayBuffer;

    env.get_double_array_region(collision, 0, arraySlice);

    let aabbs: jsize = length / 6;
    let mut shape_aabb_list: Vec<AABB> = vec![EMPTY_AABB.clone(); aabbs as usize];
    for i in 0..aabbs {
        let min_index = (i * 6) as usize;
        shape_aabb_list[i as usize] = AABB{
            min_x: arraySlice[min_index],
            min_y: arraySlice[min_index + 1],
            min_z: arraySlice[min_index + 2],
            max_x: arraySlice[min_index + 3],
            max_y: arraySlice[min_index + 4],
            max_z: arraySlice[min_index + 5]
        };
    }

    let collision_shape: VoxelShape = VoxelShape{
        aabb_list: collision_aabb_list
    };

    let shape: VoxelShape = VoxelShape{
        aabb_list: shape_aabb_list
    };

    let mut direction: NoiseDirection = NONE;
    if noised_direction == 1 {
        direction = XZ;
    } else if noised_direction == 2 {
        direction = XYZ;
    }

    let noised_collision_shape: NoisedShape = NoisedShape {
        direction,
        based_shape: collision_shape
    };

    let noised_shape: NoisedShape = NoisedShape {
        direction,
        based_shape: shape
    };
    
    minecraft::blocks::register_block_in_order(id as i32, noised_collision_shape, noised_shape);
}

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_test2(env: JNIEnv, class: jclass, worldName: jcharArray, block_x: jint, block_y: jint, block_z: jint){
    let length: jsize = env.get_array_length(worldName).unwrap();

    let mut worldNameBuffer: Vec<u16> = vec![0; length as usize];
    let worldNameSlice = &mut worldNameBuffer;

    env.get_char_array_region(worldName, 0, worldNameSlice);

    let world = minecraft::worlds::get_world(worldNameBuffer);
    let option = world.get_block_data(block_x, block_y, block_z);

    if option.is_none() {
        println!("NULL!");
        return;
    }

    println!("id = {:?}", option.unwrap().get_id());

    let collision: VoxelShape = option.unwrap().get_collision_shape().get_shape(0, 0, 0);
    let aabb_list: &Vec<AABB> = collision.get_aabb_list();
    for i in 0..aabb_list.len() {
        let aabb: &AABB = &aabb_list[i];
        println!("aabb = {:?} : min_x = {:?}", i, aabb.min_x);
        println!("aabb = {:?} : min_y = {:?}", i, aabb.min_y);
        println!("aabb = {:?} : min_z = {:?}", i, aabb.min_z);
        println!("aabb = {:?} : max_x = {:?}", i, aabb.max_x);
        println!("aabb = {:?} : max_y = {:?}", i, aabb.max_y);
        println!("aabb = {:?} : max_z = {:?}", i, aabb.max_z);
    }
}

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_setIsHigher(env: JNIEnv, class: jclass, isHigher: jboolean){
    unsafe {
        IS_HIGHER_V1_18_R1 = isHigher == 1;
        minecraft::chunk_util::set_is_higher(IS_HIGHER_V1_18_R1);
    }
}

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_getIsHigher(env: JNIEnv, class: jclass) -> jboolean{
    unsafe {
        return if IS_HIGHER_V1_18_R1 {
            1 as jboolean
        } else {
            0 as jboolean
        }
    }
}

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_addChunkData(env: JNIEnv, class: jclass, chunk_x: jint, chunk_z: jint, worldName: jcharArray, filledSections: jint, chunkData: jintArray){
    let length: jsize = env.get_array_length(worldName).unwrap();

    let mut worldNameBuffer: Vec<u16> = vec![0; length as usize];
    let worldNameSlice = &mut worldNameBuffer;

    env.get_char_array_region(worldName, 0, worldNameSlice);

    let mut sections: Vec<Option<ChunkSection>> = Vec::new();

    let max_length = minecraft::chunk_util::get_max_section_array_length();

    let mut index = 0;
    for i in 0..max_length {
        if filledSections & (1 << i) == 0 {
            sections.push(Option::None);
            continue;
        }

        let mut chunkDataBuffer: Vec<i32> = vec![0; 4096];
        let chunkDataSlice = &mut chunkDataBuffer;

        env.get_int_array_region(chunkData, (index * 4096) as jsize, chunkDataSlice);

        let mut block_pallet: Vec<Option<&'static BlockData>> = Vec::new();

        for c in 0..4096 {
            let id = chunkDataBuffer[c];

            if id < 0 {
                block_pallet.push(Option::None);
            } else {
                let block_data = minecraft::blocks::get_block_data_from_id(id);
                block_pallet.push(Option::Some(block_data));
            }
        }

        let chunk_section = ChunkSection::new(block_pallet);
        sections.push(Option::Some(chunk_section));

        index += 1;
    }

    let chunk = Chunk::new(chunk_x, chunk_z, sections);

    let world = minecraft::worlds::get_world(worldNameBuffer);

    world.set_chunk_for_single_thread(chunk);
}
