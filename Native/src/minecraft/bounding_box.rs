
#[derive(Clone)]
pub struct AABB{
    pub min_x: f64,
    pub min_y: f64,
    pub min_z: f64,
    pub max_x: f64,
    pub max_y: f64,
    pub max_z: f64,
}

impl AABB {
    pub fn is_empty(&self) -> bool {
        return self.min_x == 0.0 && self.min_y == 0.0 && self.min_z == 0.0 && self.max_x == 0.0 && self.max_y == 0.0 && self.max_z == 0.0;
    }
}

#[derive(Clone)]
pub struct VoxelShape{
    pub(crate) aabb_list: Vec<AABB>,
}

impl VoxelShape {
    pub fn shift(&self, x: f64, y: f64, z: f64) -> VoxelShape {
        let mut new_aabb_list: Vec<AABB> = Vec::new();
        for i in 0..self.aabb_list.len() {
            let aabb = &self.aabb_list[i];

            let new_aabb: AABB = AABB {
                min_x: aabb.min_x + x,
                min_y: aabb.min_y + y,
                min_z: aabb.min_z + z,
                max_x: aabb.max_x + x,
                max_y: aabb.max_y + y,
                max_z: aabb.max_z + z
            };

            new_aabb_list.push(new_aabb);
        }

        let new_shape: VoxelShape = VoxelShape {
            aabb_list: new_aabb_list
        };

        return new_shape;
    }

    pub fn get_aabb_list(&self) -> &Vec<AABB> { return &self.aabb_list; }

    pub fn is_empty(&self) -> bool {
        for i in 0..self.aabb_list.len() {
            let aabb = &self.aabb_list[i];

            if !aabb.is_empty() {
                return false;
            }
        }
        return true;
    }
}