import pandas as pd
from collections import Counter
import os
import seaborn as sns
import matplotlib.pyplot as plt
from datetime import timedelta
import numpy as np
from scipy import stats

data_dir = 'exp1'

df = pd.DataFrame()
for file in os.listdir(data_dir):
    if file.startswith('Session'):
        with open(f'{data_dir}/{file}') as f:
            starttime_millis = int(f.readline().rstrip()[17:])
        session = pd.read_csv(f'{data_dir}/{file}', skiprows=1)
    else:
        tmp = pd.read_csv(f'{data_dir}/{file}', skiprows=1)
        df = pd.concat([df, tmp], ignore_index=True)
df = df.sort_values(by=['timestamp'], ignore_index=True)

print(len(df))
# get readable time (delta from start time)
real_times = []
for time_millis in df['real time']:
    delta = (time_millis - starttime_millis) # convert to seconds
    real_times.append(str(timedelta(microseconds=delta)))
df['time (s)'] = real_times

#convert to millis
timestamp_millis = []
for time in df['timestamp']:
    new_time = str(timedelta(microseconds=(time/1e6)))
    timestamp_millis.append(new_time)

df['timestamp_millis'] = timestamp_millis

df.to_csv('processed-1.csv')