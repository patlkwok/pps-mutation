import os
import glob
import random
import datrie
import sys
from typing import *

BASE_DIR = "/home/ps2958/pps-mutation/"

def de_bruijn(k, n):
    """ de Bruijn sequence for alphabet k and subsequences of length n. """
    try:
        _ = int(k)
        alphabet = list(map(str, range(k)))

    except (ValueError, TypeError):
        alphabet = k
        k = len(k)

    a = [0] * k * n
    sequence = []

    def db(t, p):
        if t > n:
            if n % p == 0:
                sequence.extend(a[1:p + 1])
        else:
            a[t] = a[t - p]
            db(t + 1, p)
            for j in range(a[t - p] + 1, k):
                a[t] = j
                db(t + 1, t)
    db(1, 1)
    return "".join(alphabet[i] for i in sequence)

def list_mutations(directory=BASE_DIR):
    return glob.glob(directory+"/**/*.cfg", recursive=True)

def print_mutations(cfg):
    if isinstance(cfg, str): cfg = [cfg]
    
    for cf in cfg:
        m1 = Mutagen()
        m1.load_mutagen(cf)
        print(m1.patterns, m1.actions)

def visualize_change(genome, mutated):
    for idx, (c1, c2) in enumerate(zip(genome, mutated)):
        print(c1 if c1==c2 else f"\x1b[31m{c1}\x1b[0m", end='')
        if idx % 100 == 99: print()
    
    print()
    for idx, (c1, c2) in enumerate(zip(genome, mutated)):
        print(c1 if c1==c2 else f"\x1b[32m{c2}\x1b[0m", end='')
        if idx % 100 == 99: print()
    
    if len(genome) % 100 != 0:
        print()

def generate_random_genome(length=1000):
    return ''.join(['actg'[random.randint(0,3)] for _ in range(length)])

def identify_intervals(genome, mutated, m):
    """
    Inspired by Agglomerative Heirarchical Clustering
    
    :genome
        The original sequence of genes
    :mutated
        The mutated sequence of genes
    :m
        Number of mutations performed by the mutagen
    
    Returns
    _______
        The most likely sequence of indices of mutated regions,
        based on heuristic = length of mutation
    """
    if m == 0: return []
    
    diffs = [(idx, idx) for idx, (c1, c2) in enumerate(zip(genome, mutated)) if c1 != c2]
    
    while len(diffs) > m:
        min_merge_length, minidx = min(
            (diffs[i][1]-diffs[i-1][0], i)
            for i in range(1, len(diffs))
        )
        
        diffs[minidx-1] = (diffs[minidx-1][0], diffs.pop(minidx)[1])
    
    return diffs

class Mutagen:
    def __init__(self, patterns:List[str]=None, actions:List[str]=None):
        self.patterns = patterns or []
        self.actions  = actions or []
        self.num_experiments = 0
    
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
        self.num_experiments += 1
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
                tmp_idx = (start+k) % length
                
                # Apply action
                new_mutable = mutable[:]
                for j, ach in enumerate(action):
                    idx = (start + k + j) % length
                    
                    if '0' <= ach <= '9':
                        new_mutable[idx] = mutable[(start + k + int(ach))%length]
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

def convert(x):
    return ''.join(y if f"{y}" in "actg" else '0123456789ABCDEFGHIJ'[y] for y in x) or "_"

def convert_hypothesis(hypothesis):
    return " ".join([convert(tmp) for tmp in hypothesis]) 

def print_hypothesis(hypothesis):
    print(convert_hypothesis(hypothesis))

def parse_offset(elements, offset):
    offset = abs(offset)
    return set(e if isinstance(e, str) else e-offset for e in elements if isinstance(e, str) or e>=offset)

def find_actions(genome, result, mp, initial_hypotheses=None):
    intervals = identify_intervals(genome, result, mp)
    intlen = len(intervals)
    
    separated = []
    combined  = []
    
    for i in range(intlen):
        if (i>0 and intervals[i][0] - intervals[i-1][0] < 10) or \
            (i<intlen-1 and intervals[i+1][0] - intervals[i][0] < 10):
            combined.append(intervals[i])
        else:
            separated.append(intervals[i])
    
    print(separated)
    print(combined)
    
    hypotheses = initial_hypotheses
    for intmin, intmax in separated:
        if intmin > 985: continue
        diff = intmax - intmin + 1
        buflen = 10 - diff

        print(diff, buflen, buflen*2+diff)
        visualize_change(genome[intmin-buflen:intmax+buflen+1], result[intmin-buflen:intmax+buflen+1])
        
        enumidxes = {}
        for key in 'atcg':
            enumidxes[key] = [i for i in range(buflen*2+diff) if genome[intmin-buflen+i]==key]
        
        hypothesis = []
        for idx in range(intmin-buflen, intmax+buflen+1):
            key = result[idx]
            hypothesis.append(set([key] + enumidxes[key]))

        print(f"CURRENT HYPOTHESIS : {len(hypothesis)} ({intmin}, {intmax}, {buflen} => {diff}) [{convert_hypothesis(hypothesis)}]"
              f" ({buflen}, {buflen+diff-1})")

        if hypotheses is None:
            hypotheses = [(hypothesis, buflen, buflen+diff-1)]

        new_hypotheses = []
        for prev_hyp, prev_start, prev_end in hypotheses:
            print(f"PREVIOUS HYPOTHESIS : {len(prev_hyp)} [{convert_hypothesis(prev_hyp)}] ({prev_start}, {prev_end})")

            for offset in range(buflen, -prev_start-1, -1):
                if offset >= 0:
                    prev = [set(elem) for elem in prev_hyp]
                    curr = [parse_offset(elem, offset) for elem in hypothesis[offset:]]
                else:
                    prev = [parse_offset(elem, offset) for elem in prev_hyp[-offset:]]
                    curr = [set(elem) for elem in hypothesis]

                combined = [a.intersection(b) for a, b in zip(prev, curr)]
                
                if not combined: continue

                if len(min(combined, key=lambda x:len(x))) > 0:
                    print("Offset ", offset)
                    print("Prev : ", convert_hypothesis(prev), len(prev))
                    print("Curr : ", convert_hypothesis(curr), len(curr))
                    print("Comb : ", convert_hypothesis(combined), len(curr))

                    hypotheses = (tuple(combined), prev_start+offset if offset<0 else buflen-offset, prev_end if offset>=0 else buflen+diff-1)
                    print(f"FOUND HYPOTHESIS {offset}:  {len(combined)} [{convert_hypothesis(combined)}] ({hypotheses[1]}, {hypotheses[2]})")
                    print()

                    new_hypotheses.append(hypotheses)
        
        # Reduce new hypotheses using sets
        hypotheses = set(
            (tuple(tuple(h) for h in hyp[0]), hyp[1], hyp[2])
            for hyp in new_hypotheses
        )
        
        print("Number of possible hypotheses = ", len(hypotheses))
        for hyp in hypotheses:
            print(f"{hyp[1]} {hyp[2]} [{convert_hypothesis(hyp[0])}]")
        
        print()
        
    return hypotheses

if __name__ == '__main__':
    # mute = Mutagen()
    # mute.add('actg;actg;t;a;g', '1234')

    # m = 10
    # action = None

    # for i in range(1000):
    #     genome = generate_random_genome(1000)
    #     result = mute.mutate(genome, m)
    #     mp = mute.getNumberOfMutations()
        
    #     # print(f"{mp} changes found.")
    #     # visualize_change(genome, result)

    #     intervals = identify_intervals(genome, result, mp)
    #     action = find_actions(genome, result, mp, action)

    #     if action is None:
    #         continue
            
    #     if len(action) == 0:
    #         # Something went wrong
    #         action = None
    #         continue
            
    #     # for hyp in action:
    #     #     print(f"{hyp[1]} {hyp[2]} [{convert_hypothesis(hyp[0])}]")
        
    #     # if len(action) == 1 and action_potential(list(action)[0]) < 20:
    #     if len(action) == 1:
    #         break

    # print(mute.num_experiments, action)
    
    # print("Action: ", action)
    # print("Intervals: ", identify_intervals(genome, result, mp))
        
    print(identify_intervals(sys.argv[1], sys.argv[2], int(sys.argv[3])))