static mut IS_HIGHER_V1_18_R1: bool = true;

pub fn set_is_higher(is: bool) {
    unsafe {
        IS_HIGHER_V1_18_R1 = is;
    }
}

pub fn is_higher_v1_18_r1() -> bool {
    unsafe {
        IS_HIGHER_V1_18_R1
    }
}

pub fn get_max_section_array_length() -> usize {
    unsafe {
        return if IS_HIGHER_V1_18_R1 {
            24
        } else {
            16
        }
    }
}

pub fn get_section_index(block_y: i32) -> usize {
    unsafe {
        return if IS_HIGHER_V1_18_R1 {
            ((block_y + 64) >> 4) as usize
        } else {
            (block_y >> 4) as usize
        }
    }
}