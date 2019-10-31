#!/usr/bin/env python

import csv
import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats
import math

keys = []
means = {}
sds = {}

with open('results/summary.csv') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')
    next(reader, None)
    for i, (mean, sd) in enumerate(reader, start=1):
        key = str(i)
        keys.append(key)
        means[key] = float(mean.strip())
        sds[key] = float(sd.strip())

legends = []

colors = plt.rcParams['axes.prop_cycle']()
fig, axes = plt.subplots(2, 2)

for (key, ax) in list(zip(keys, axes.flatten())):
    mean, sd = means[key], sds[key]
    x = np.linspace(mean - 3*sd, mean + 3*sd, 100)
    ax.plot(x, stats.norm.pdf(x, mean, sd), **next(colors), label=key)
    ax.legend()


plt.savefig('graph_multi.png', dpi=300, bbox_inches='tight')
plt.show()
