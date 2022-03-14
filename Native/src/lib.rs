mod minecraft;

extern crate jni;

use jni::sys::{jboolean, jchar, jcharArray, jclass, jdouble, jdoubleArray, jint, jintArray, jsize, jstring};
use jni::JNIEnv;
use jni::*;
use crate::minecraft::blocks::{BlockData, register_block_in_order};
use crate::minecraft::bounding_box::{AABB, VoxelShape};

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
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_registerBlockInOrder(env: JNIEnv, class: jclass, id: jint, collision: jdoubleArray){
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
    let mut aabb_list: Vec<AABB> = vec![EMPTY_AABB.clone(); aabbs as usize];
    for i in 0..aabbs {
        let min_index = (i * 6) as usize;
        aabb_list[i as usize] = AABB{
            min_x: arraySlice[min_index],
            min_y: arraySlice[min_index + 1],
            min_z: arraySlice[min_index + 2],
            max_x: arraySlice[min_index + 3],
            max_y: arraySlice[min_index + 4],
            max_z: arraySlice[min_index + 5]
        };
    }

    let collision_shape: VoxelShape = VoxelShape{
        aabb_list
    };
    
    minecraft::blocks::register_block_in_order(id as i32, collision_shape);
}

#[no_mangle]
#[allow(unused_variables, non_snake_case)]
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_test2(env: JNIEnv, class: jclass, id: jint){
    let data: &BlockData = minecraft::blocks::get_block_data_from_id(id as i32);

    let collision: &VoxelShape = &data.collision_shape;
    let aabb_list: &Vec<AABB> = &collision.aabb_list;
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
pub extern "system" fn Java_thpmc_engine_api_natives_NativeBridge_addChunkData(env: JNIEnv, class: jclass, worldName: jcharArray, filledSections: jint, chunkData: jintArray){
    let length: jsize = env.get_array_length(chunkData).unwrap();

    let mut arrayBuffer: Vec<jchar> = vec![0; length as usize];
    let arraySlice = &mut arrayBuffer;

    env.get_char_array_region(chunkData, 0, arraySlice);
}
