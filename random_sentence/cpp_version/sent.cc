#include<vector>
#include<string>
#include<iostream>
#include<sstream>
#include<iterator>
#include<algorithm>
#include "select.hh"

using namespace std;

struct rule {
   string name;
   vector<string> expansions;
};

template<typename O>
void expand(const vector<rule> &g, const string &s, O &tgt) {
   istringstream strm { s };
   istream_iterator<string> eos, in { strm };
   for_each(in, eos, [&](const string &word) {
      auto term = find_if(begin(g), end(g), [&](const rule &r) { return r.name == word; });
      if(term != end(g)) {
         auto selected = select_randomly(begin(term->expansions), end(term->expansions));  
         expand(g, *selected, tgt);
      } else {
         *tgt++ = word;
      }
   });
}

int main(int argc, char **argv) {
   vector<rule> grammar { 
       { "S"    , { "NP VP", "S and S" } },
       { "NP"   , { "Art N", "Name" } },
       { "VP"   , { "V NP" } },
       { "Art"  , { "the", "a", "every", "some" } },
       { "N"    , { "man", "ball", "woman", "table", "dog", "cat", "wombat" } },
       { "V"    , { "hit", "took", "saw", "liked", "worshiped", "remembered" } },
       { "Name" , { "Alice", "Bob", "Carlos", "Dan", "Eve" } }
   };

  ostream_iterator<string> oit(cout, " ");
  expand(grammar, "S", oit);
  cout << endl;
  return 0;
}
