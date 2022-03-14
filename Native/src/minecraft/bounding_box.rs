
#[derive(Clone)]
pub struct AABB{
    pub min_x: f64,
    pub min_y: f64,
    pub min_z: f64,
    pub max_x: f64,
    pub max_y: f64,
    pub max_z: f64,
}

#[derive(Clone)]
pub struct VoxelShape{
    pub aabb_list: Vec<AABB>,
}