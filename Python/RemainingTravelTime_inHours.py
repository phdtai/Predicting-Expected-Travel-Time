#!/usr/bin/env python
# coding: utf-8

# In[31]:


import pandas as pd
import geopy.distance
from datetime import datetime

dataset = pd.read_csv(r'C:\Users\Jannis\Felix-Rotter.csv',
                     usecols=['TripID', 'CurrentDistance', 'time','StartLatitude', 'StartLongitude', 'EndLatitude', 
                              'EndLongitude', 'StartTime', 'EndTime', 'Latitude', 'Longitude', 'SOG', 'shiptype'])
dataset.insert(2, 'RemainingTravelTime', 0.0)
dataset.sort_values(['TripID', 'time'])


# In[32]:


from datetime import datetime
for i in range (0, len(dataset)):
    time1 = dataset['EndTime'].values[i]
    time2 = dataset['time'].values[i]
    d1 = datetime.strptime(time1, "%Y-%m-%d %H:%M")
    d2 = datetime.strptime(time2, "%Y-%m-%d %H:%M")
    result = d1 - d2
    duration_in_s = result.total_seconds()
    hours = divmod(duration_in_s, 3600)[0]
    dataset['RemainingTravelTime'].values[i] = hours

dataset.sort_values(['TripID', 'time'])


# In[33]:


dataset.to_csv('Felix-Rotter-RemainingTravelTime_InHours.csv')


# In[ ]:




