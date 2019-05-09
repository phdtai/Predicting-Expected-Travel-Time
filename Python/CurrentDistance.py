#!/usr/bin/env python
# coding: utf-8

# In[49]:


import pandas as pd
import geopy.distance

dataset = pd.read_csv(r'C:\Users\Jannis\Desktop\group03-master\csv_result-felixstowe_rotterdam.csv',
                     usecols=['TripID', 'time','StartLatitude', 'StartLongitude', 'EndLatitude', 
                              'EndLongitude', 'StartTime', 'EndTime', 'Latitude', 'Longitude', 'SOG', 
                              'Destination', 'shiptype'])
dataset.insert(1, 'CurrentDistance', 0)
dataset.sort_values(['TripID', 'time'])


# In[54]:


for i in range(0, len(dataset)):
    end_lat = dataset['EndLatitude'].values[i]
    end_long = dataset['EndLongitude'].values[i]
    coords_1 = (end_lat, end_long)
    current_lat = dataset['Latitude'].values[i]
    current_long = dataset['Longitude'].values[i]
    coords_2 = (current_lat, current_long)
    distance = geopy.distance.distance(coords_1, coords_2).km
    dataset['CurrentDistance'].values[i] = distance
    
print(dataset)


# In[ ]:


dataset.to_csv('Felix-Rotter-distance.csv')

