
pub enum FluidCollisionMode {
    Never = 0,
    SourceOnly = 1,
    Always = 2
}


pub struct CollideOption {
    pub fluid_collision_mode: FluidCollisionMode,
    pub is_ignore_passable_block: bool
}