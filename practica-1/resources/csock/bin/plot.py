#!/usr/bin/env python

import csv
import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats
import math

bss = []
means = {}
sds = {}

with open('results/summary.csv') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')
    next(reader, None)
    for (bs, mean, sd) in reader:
        key = bs.strip()
        bss.append(key)
        means[key] = float(mean.strip())
        sds[key] = float(sd.strip())

legends = []

for bs in bss:
    mean, sd = means[bs], sds[bs]
    x = np.linspace(mean - 3*sd, mean + 3*sd, 100)
    plt.plot(x, stats.norm.pdf(x, mean, sd))
    legends.append(bs)

plt.legend(legends)

plt.savefig('graph.png', dpi=300, bbox_inches='tight')
plt.show()
