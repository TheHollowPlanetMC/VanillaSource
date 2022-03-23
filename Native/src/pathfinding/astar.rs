use std::collections::{HashMap, HashSet};
use crate::{BlockData, Chunk, minecraft};
use crate::collision::collisions::CollideOption;
use crate::minecraft::worlds::{GlobalWorld, LocalWorld};

#[derive(PartialEq, Eq, Hash, Clone)]
pub struct BlockPosition {
    pub x: i32,
    pub y: i32,
    pub z: i32
}

impl BlockPosition {
    pub(crate) fn new(x: i32, y: i32, z: i32) -> Self {
        Self {
            x,
            y,
            z
        }
    }
}


#[derive(PartialEq, Eq, Hash, Clone)]
pub struct NodeData{
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub actual_cost: i32,
    pub estimated_cost: i32,
    pub score: i32,
    pub origin: Box<Option<NodeData>>
}

impl NodeData {
    pub fn new(x: i32, y: i32, z: i32, actual_cost: i32, estimated_cost: i32, origin: Option<NodeData>) -> Self {
        Self {
            x,
            y,
            z,
            actual_cost,
            estimated_cost,
            score: actual_cost + estimated_cost,
            origin: Box::new(origin)
        }
    }

    pub fn get_position(&self) -> BlockPosition {
        return BlockPosition::new(self.x, self.y, self.z);
    }

    pub fn get_neighbours(&self, down: i32, up: i32, world: &mut LocalWorld, collide_option: &CollideOption) -> Vec<BlockPosition> {
        let mut neighbour: Vec<BlockPosition> = Vec::new();

        let p1 = self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x + 1, self.y, self.z), world, collide_option);
        let p2 = self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x, self.y, self.z + 1), world, collide_option);
        let p3 = self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x - 1, self.y, self.z), world, collide_option);
        let p4 = self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x, self.y, self.z - 1), world, collide_option);

        if p1 || p2 {
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x + 1, self.y, self.z + 1), world, collide_option);
        }
        if p2 || p3 {
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x - 1, self.y, self.z + 1), world, collide_option);
        }
        if p3 || p4 {
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x - 1, self.y, self.z - 1), world, collide_option);
        }
        if p4 || p1 {
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x + 1, self.y, self.z - 1), world, collide_option);
        }


        for uh in 1..up+1 {
            if !is_traversable(world, self.x, self.y + uh, self.z, collide_option) {
                break;
            }
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x + 1, self.y + uh, self.z), world, collide_option);
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x, self.y + uh, self.z + 1), world, collide_option);
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x - 1, self.y + uh, self.z), world, collide_option);
            self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x, self.y + uh, self.z - 1), world, collide_option);
        }

        let mut da = true;
        let mut db = true;
        let mut dc = true;
        let mut dd = true;
        for dy in 1..down+1 {
            if da {
                if is_traversable(world, self.x + 1, self.y - dy + 1, self.z, collide_option) {
                    self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x + 1, self.y - dy, self.z), world, collide_option);
                }else {
                    da = false;
                }
            }
            if db {
                if is_traversable(world, self.x, self.y - dy + 1, self.z + 1, collide_option) {
                    self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x, self.y - dy, self.z + 1), world, collide_option);
                }else {
                    db = false;
                }
            }
            if dc {
                if is_traversable(world, self.x - 1, self.y - dy + 1, self.z, collide_option) {
                    self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x - 1, self.y - dy, self.z), world, collide_option);
                }else {
                    dc = false;
                }
            }
            if dd {
                if is_traversable(world, self.x, self.y - dy + 1, self.z - 1, collide_option) {
                    self.add_if_can_stand(&mut neighbour, BlockPosition::new(self.x, self.y - dy, self.z - 1), world, collide_option);
                }else {
                    dd = false;
                }
            }
        }

        return neighbour;
    }

    pub fn add_if_can_stand(&self, positions: &mut Vec<BlockPosition>, position: BlockPosition, world: &mut LocalWorld, collide_option: &CollideOption) -> bool {
        return if can_stand(world, position.x, position.y, position.z, collide_option) {
            positions.push(position);
            true
        } else {
            false
        }
    }

}


pub fn can_stand(world: &mut LocalWorld, x: i32, y: i32, z: i32, collide_option: &CollideOption) -> bool{
    let mut option = world.get_chunk_at(x >> 4, z >> 4);
    if option.is_none() {
        return false;
    }

    let chunk = option.unwrap();
    let block1 = chunk.get_block_data(x, y, z);
    let block2 = chunk.get_block_data(x, y + 1, z);
    let block3 = chunk.get_block_data(x, y - 1, z);

    let mut traversable = false;
    if block1.is_none() && block2.is_none() {
        traversable = true;
    }else if block1.is_none(){
        traversable = !chunk.has_collision(x, y + 1, z, collide_option);
    }else if block2.is_none(){
        traversable = !chunk.has_collision(x, y, z, collide_option);
    }else{
        traversable = !chunk.has_collision(x, y, z, collide_option) && !chunk.has_collision(x, y + 1, z, collide_option);
    }

    if !traversable {
        return false;
    }

    if block3.is_none() {
        return false;
    }

    return chunk.has_collision(x, y - 1, z, collide_option);
}


pub fn is_traversable(world: &mut LocalWorld, x: i32, y: i32, z: i32, collide_option: &CollideOption) -> bool{
    let mut option = world.get_chunk_at(x >> 4, z >> 4);
    if option.is_none() {
        return true;
    }

    let chunk = option.unwrap();
    let block1 = chunk.get_block_data(x, y, z);
    let block2 = chunk.get_block_data(x, y + 1, z);

    return if block1.is_none() && block2.is_none() {
        true
    } else if block1.is_none() {
        !chunk.has_collision(x, y + 1, z, collide_option)
    } else if block2.is_none() {
        !chunk.has_collision(x, y, z, collide_option)
    } else {
        !chunk.has_collision(x, y, z, collide_option) && !chunk.has_collision(x, y + 1, z, collide_option)
    }
}



pub fn run_astar(world: &mut LocalWorld, start: BlockPosition, goal: BlockPosition, down_height: i32, jump_height: i32, max_iteration: i32, collide_option: CollideOption) -> Vec<BlockPosition> {
    if start.eq(&goal) {
        //I couldn't find a path...
        return Vec::new();
    }

    let mut node_map: HashMap<BlockPosition, NodeData> = HashMap::new();
    let mut opened_positions: HashSet<BlockPosition> = HashSet::new();
    let mut node_list: HashSet<NodeData> = HashSet::new();

    //Open first position node
    let mut start_node = open_node(Option::None, start, &goal, &mut node_map, &mut opened_positions);
    opened_positions.remove(&start_node.get_position());

    //Current node
    let mut current_node = start_node.clone();

    //Nearest node
    let mut nearest_node = current_node.clone();

    //Iteration count
    let mut iteration = 0;

    //Start pathfinding
    loop {
        iteration += 1;

        //Max iteration check
        if iteration >= max_iteration {
            //Give up!
            let mut paths: Vec<BlockPosition> = Vec::new();
            paths.push(nearest_node.get_position());
            get_paths(nearest_node, &mut paths);
            paths.reverse();

            return paths;
        }

        let neighbours = current_node.get_neighbours(down_height, jump_height, world, &collide_option);

        for block_position in neighbours.iter() {
            //Check if closed
            let new_node = open_node(Option::Some(&current_node), (*block_position).clone(), &goal, &mut node_map, &mut opened_positions);
            if !opened_positions.contains(&new_node.get_position()) {
                continue;
            }

            //Update nearest node
            if new_node.estimated_cost < nearest_node.estimated_cost {
                nearest_node = new_node.clone();
            }

            node_list.insert(new_node);
        }

        //Close node
        opened_positions.remove(&current_node.get_position());
        node_list.remove(&current_node);

        if node_list.len() == 0 {
            //I couldn't find a path...
            let mut paths: Vec<BlockPosition> = Vec::new();
            paths.push(nearest_node.get_position());
            get_paths(nearest_node, &mut paths);
            paths.reverse();

            return paths;
        }

        //Choose next node
        let mut score = i32::MAX;
        for node_data in node_list.iter() {
            if node_data.score < score {
                score = node_data.score;
                current_node = (*node_data).clone();
            } else if node_data.score == score {
                if node_data.estimated_cost < current_node.estimated_cost {
                    current_node = (*node_data).clone();
                } else if node_data.estimated_cost == current_node.estimated_cost {
                    if node_data.actual_cost <= current_node.actual_cost {
                        current_node = (*node_data).clone();
                    }
                }
            }
        }

        //Check goal
        if current_node.get_position().eq(&goal) {
            let mut paths: Vec<BlockPosition> = Vec::new();
            paths.push(current_node.get_position());
            get_paths(current_node, &mut paths);
            paths.reverse();

            return paths;
        }
    }
}

fn get_paths(node_data: NodeData, paths: &mut Vec<BlockPosition>) {
    let origin = node_data.origin;
    if origin.is_none() {
        return;
    }

    paths.push(origin.as_ref().as_ref().unwrap().get_position());
    get_paths(origin.unwrap(), paths);
}

fn open_node(origin: Option<&NodeData>, block_position: BlockPosition, goal: &BlockPosition, node_map: &mut HashMap<BlockPosition, NodeData>, opened_positions: &mut HashSet<BlockPosition>) -> NodeData {
    let node_data = node_map.get(&block_position);
    if node_data.is_some() {
        return (*node_data.unwrap()).clone();
    }

    //Calculate actual cost
    let actual_cost = if origin.is_none() {
        0
    } else {
        origin.unwrap().actual_cost + 1
    };

    //Calculate estimated cost
    let estimated_cost = (goal.x - block_position.x).abs() + (goal.y - block_position.y).abs() + (goal.z - block_position.z).abs();

    let origin_node = if origin.is_some() {
        Option::Some((*origin.unwrap()).clone())
    } else {
        Option::None
    };

    let node = NodeData::new(block_position.x, block_position.y, block_position.z, actual_cost, estimated_cost, origin_node);
    opened_positions.insert(block_position.clone());

    node_map.insert(block_position.clone(), node.clone());
    return node;
}