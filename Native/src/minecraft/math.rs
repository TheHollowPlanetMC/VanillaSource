
pub fn create_plant_noise(block_x: i32, block_y: i32, block_z: i32) -> i64{
    let mut temp: i64 = ((block_x as i64) * 3129871_i64) ^ (block_z as i64) * 116129781_i64 ^ (block_y as i64);
    temp = temp * temp * 42317861_i64 + temp * 11_i64;
    return temp >> 16;
}

pub fn get_chunk_key(chunk_x: i32, chunk_z: i32) -> i64 {
    ((chunk_z as i64) << 32) | ((chunk_x as i64) & 0xFFFFFFFF)
}

pub fn get_yzx_index(x: i32, y: i32, z: i32) -> i32 {
    return y << 8 | z << 4 | x;
}