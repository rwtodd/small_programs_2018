#include<vector>
#include<string>
#include<iostream>
#include<sstream>
#include<iterator>
#include<algorithm>
#include<map>
#include "select.hh"

using namespace std;
using grammar = map<string, vector<string>>; // Map from NonTerminal to Expansions

template<typename O>
void expand(const grammar &g, const string &s, O &tgt) {
   istringstream strm { s };
   istream_iterator<string> eos, in { strm };
   for_each(in, eos, [&](auto &word) {
      auto term = g.find(word);
      if(term != g.end()) {
         auto selected = select_randomly(begin(term->second), end(term->second));
         expand(g, *selected, tgt);
      } else {
         *tgt++ = word;
      }
   });
}

int main(int argc, char **argv) {
   grammar g {
       { "S"    , { "NP VP", "S and S" } },
       { "NP"   , { "Art N", "Name" } },
       { "VP"   , { "V NP" } },
       { "Art"  , { "the", "a", "every", "some" } },
       { "N"    , { "man", "ball", "woman", "table", "dog", "cat", "wombat" } },
       { "V"    , { "hit", "took", "saw", "liked", "worshiped", "remembered" } },
       { "Name" , { "Alice", "Bob", "Carlos", "Dan", "Eve" } }
   };

  ostream_iterator<string> oit(cout, " ");
  expand(g, "S", oit);
  cout << endl;
  return 0;
}
