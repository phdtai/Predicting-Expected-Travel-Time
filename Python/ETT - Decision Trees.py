#!/usr/bin/env python
# coding: utf-8

# In[44]:


import pandas as pd
import geopy.distance

dataset = pd.read_csv(r'C:\Users\Jannis\Felix-Rotter-updated1.csv',
                     usecols=['TripID', 'CurrentDistance', 'RemainingTravelTime','time','StartLatitude', 'StartLongitude', 'EndLatitude', 
                              'EndLongitude', 'Latitude', 'Longitude', 'SOG', 'shiptype'])
dataset.sort_values(['TripID', 'time'])


# In[45]:


from sklearn.tree import DecisionTreeClassifier # Import Decision Tree Classifier
from sklearn.model_selection import train_test_split # Import train_test_split function
from sklearn import metrics #Import scikit-learn metrics module for accuracy calculation
from datetime import datetime

feature_cols = ['CurrentDistance', 'shiptype', 'SOG']

X = dataset[feature_cols]
y = dataset.RemainingTravelTime # Target variable
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=1)


# In[46]:


# Create Decision Tree classifer object
clf = DecisionTreeClassifier()

# Train Decision Tree Classifer
clf = clf.fit(X_train,y_train)

#Predict the response for test dataset
y_pred = clf.predict(X_test)


# In[55]:


print("Accuracy:",metrics.accuracy_score(y_test, y_pred))


# In[49]:


from sklearn.tree import export_graphviz
from sklearn.externals.six import StringIO  
from IPython.display import Image  
import pydotplus

export_graphviz(clf, out_file='result1.dot',  
                filled=True, rounded=True,
                special_characters=True,feature_names = feature_cols)


# In[54]:

# Testing if prediction works
test = clf.predict([[184, 71, 9.2]])
print(test)





