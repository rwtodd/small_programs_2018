extern crate rand;

use std::collections::HashMap;
use rand::prelude::*;

#[derive(Debug)]
enum Tree {
   Terminal(String),               // atomic word
   NonTerminal(String, Vec<Tree>)  // expandable term
}

// expand a term `k` according to the rules in `g` into a `Tree`
fn expand_tree(g: &HashMap<&str,Vec<&str>>, k: &str) -> Tree {
    let name = k.to_string();
    match g.get(k) {
       Some(rhs) => Tree::NonTerminal(name,
                                      thread_rng().choose(&rhs)
                                                  .unwrap()
                                                  .split_whitespace()
                                                  .map(|word| expand_tree(g, word))
                                                  .collect()),
       None      => Tree::Terminal(name)
    }
}

// expand a term `k` according to the rules in `g`, pushing atomic words
// into `out`
fn expand(g: &HashMap<&str,Vec<&str>>, k: &str, out: &mut Vec<String>) {
    match g.get(k) {
       Some(rhs) => thread_rng().choose(&rhs)
                                .unwrap()
                                .split_whitespace()
                                .for_each(|word| expand(g, word, out)),
       None      => out.push(k.to_string())
    }
}

fn main() {
    let mut grammar = HashMap::new();
    grammar.insert("S"   , vec!["NP VP","S and S"]);
    grammar.insert("NP"  , vec!["Art N","Name"]);
    grammar.insert("VP"  , vec!["V NP"]);
    grammar.insert("Art" , vec!["the","a","every","some"]);
    grammar.insert("N"   , vec!["man","ball","woman","table","dog","cat","wombat"]);
    grammar.insert("V"   , vec!["hit","took","saw","liked","worshiped","remembered"]);
    grammar.insert("Name", vec!["Alice","Bob","Carlos","Dan","Eve"]);

    let mut output = Vec::new();
    expand(&grammar, "S", &mut output); 
    output.iter()
          .for_each(|word| print!("{} ", word));

    println!(); println!();

    println!("{:?}",expand_tree(&grammar, "S"));
}
