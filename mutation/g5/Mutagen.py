import random
from typing import *

class Mutagen:
    def __init__(self, patterns:List[str]=None, actions:List[str]=None):
        self.patterns = patterns or []
        self.actions  = actions or []
    
    def load_mutagen(self, filename:str):
        with open(filename) as handle:
            for line in handle:
                line = line.strip()
                if '@' not in line: continue
                
                pattern, action = line.split('@')
                
                self.patterns.append(pattern)
                self.actions.append(action)
    
    def match(self, s:str, pattern:str) -> int:
        l = pattern.split(';')
        
        for i in range(len(s) - len(l)):
            
            for j, pattern_ch in enumerate(l):
                if s[i+j] not in pattern_ch:
                    break
            else:
                return i
        
        return -1
 
    def mutate(self, genome:str, m:int) -> str:
        length  = len(genome)
        mutable = list(genome)
        
        for numMutations in range(m):
            start = random.randint(0, length)
            rotated_genome = (mutable[start:] + mutable[:start]) * 2
            
            pattern_action_pairs = list(zip(self.patterns, self.actions))
            random.shuffle(pattern_action_pairs)
            
            for pattern, action in pattern_action_pairs:
                k = self.match(rotated_genome, pattern)
                
                # Mutation pattern not found
                if k == -1: continue
                
                # Apply action
                new_mutable = mutable[:]
                for j, ach in enumerate(action):
                    idx = (start + k + j) % length
                    
                    if '0' <= ach <= '9':
                        new_mutable[idx] = mutable[start + int(ach)]
                    else:
                        new_mutable[idx] = ach
                
                if new_mutable != mutable:
                    mutable = new_mutable
                    break
                
            else:
                # Never broke out of loop, meaning no change was done
                self.num_mutations = numMutations;
                return ''.join(mutable)
        
        self.num_mutations = m;
        return ''.join(mutable)
    
    def getNumberOfMutations(self) -> int:
        return self.num_mutations
    
    def add(self, pattern:str, action:str):
        self.patterns.append(pattern)
        self.actions.append(action)
    
    def remove(self, i:int):
        if i>0 and i<patterns.size():
            self.patterns.remove(i)
            self.actions.remove(i)
    
    def getPatterns(self): return self.patterns
    def getActions(self): return self.actions
    def getPatternActionPairs(self):
        return [f"{pattern}@{action}" for pattern, action in zip(self.patterns, self.actions)]
    
    def __eq__(self, other):
        if len(self.patterns) != len(other.patterns):
            return False
        
        return set(self.getPatternActionPairs()) == set(other.getPatternActionPairs())

    def translate(c:str) -> int:
        return {'a':0, 'c':1, 'g':2, 't':3}.get(c, -1)

if __name__ == "__main__":
    # Three ways of creating a mutagen
    
    # 1. Using add()
    m1 = Mutagen()
    m1.add("a;c;g;t", "aaaa")
    
    # 2. Using constructor
    m2 = Mutagen(patterns=['a;c;g;t', 'ac;actg;g', 'a;c;c'], actions=['aaaa', 'a11', 'att'])
    
    # 3. Load from file
    m3 = Mutagen()
    m3.load_mutagen("../../mutagen.cfg")