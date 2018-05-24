#include<vector>
#include<string>
#include<iostream>
#include<sstream>
#include<algorithm>
#include "select.hh"

using namespace std;

struct rule {
   string name;
   vector<string> expansions;
};

string expand(vector<rule> &g, const string &s) {
   istringstream in {s};
   ostringstream out;
   bool first = true;
   while(in) {
      string word;
      in >> word;
      if(word.size() == 0) break;
      if(!first) { out << ' '; }

      auto term = find_if(begin(g), end(g), [&](rule &r) { return r.name == word; });
      if(term != end(g)) {
         auto selected = select_randomly(begin(term->expansions), end(term->expansions));  
         out << expand(g, *selected);
      } else {
         out << word;
      }

      first = false;
   }
   return out.str();
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

  cout << expand(grammar, "S") << endl;
  return 0;
}
