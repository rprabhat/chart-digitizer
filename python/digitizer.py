# -*- coding: utf-8 -*-
"""
Created on Wed Nov 22 10:12:35 2017
TODO

# For batch generator and multiproc c.f.
    https://gist.github.com/tdeboissiere/195dde7fddfcf622a82a895b90d2c800
# SEE ATHENA >> /examples/research/cbb_keras_tensorflow.py

# https://keras.io/getting-started/sequential-model-guide/
# =============================================================================

# see for GPU  https://keras.io/utils/#multi_gpu_model

@author: i015225
"""

# In[Headless Server for faster img generation]
import matplotlib
matplotlib.use("Agg")

# In[139]:
#c=get_ipython()
#%config InlineBackend.figure_format = 'svg'
#%pprint #Toggle
# get_ipython().magic(u'matplotlib inline')

import pandas as pd
from pandas.api.types import is_bool_dtype, is_categorical, infer_dtype, is_categorical_dtype
import numpy as np

import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from matplotlib.dates import AutoDateLocator, AutoDateFormatter, date2num
from matplotlib.dates import DateFormatter
import seaborn as sns

import time
from datetime import datetime, timedelta
import itertools
import statsmodels.api as sm
import warnings

warnings.filterwarnings("ignore", category=DeprecationWarning)
warnings.filterwarnings("ignore", category=FutureWarning)

import os
import sys
from sklearn import svm
from sklearn.metrics import accuracy_score, make_scorer

# Import the scikit-learn function to confusion matrix.
from sklearn.metrics import confusion_matrix
# Import the scikit-learn function to compute error.
from sklearn.metrics import mean_squared_error

import matplotlib
from pandas.tseries.offsets import BDay

import keras
from keras import backend as K

#import tensorflow as tf
#import numpy
from keras.models import Sequential
from keras.utils.np_utils import to_categorical
from keras.layers import LSTM, Dense, Dropout, Flatten, Embedding, TimeDistributed
from keras.wrappers.scikit_learn import KerasRegressor
from keras import metrics
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import KFold
from sklearn.preprocessing import StandardScaler ,quantile_transform
from sklearn.pipeline import Pipeline


# In[Charts]:
 # -*- coding: utf-8 -*-
"""
Created on Tue Jun 26 15:04:00 2018

@author: i015225
"""


#import numpy as np
#import matplotlib.pyplot as plt
#import pandas as pd
#import seaborn as sns
#import io
#


def img_gen_bar():
    """ Generate a random bar chart exporting height array and pixel array
        TODO:
        TODO: Add options for randomising scale, # bars, showing axis, grids, rotating labels, legends etc.
    """
    data = pd.DataFrame(data=np.random.rand(5,1), index=range(1,6), columns=['Fred'])
    m,n = np.shape(data)

    plt.clf()
    plt.bar(x=data.index.values, height=data.values.ravel(), color='k') # figsize=(10, 6))
    # Options for later from https://matplotlib.org/api/_as_gen/matplotlib.pyplot.bar.html
    # bar_width = 0.35
    # alpha = .3
    fig=plt.gcf()
    fig.set_size_inches(3, 2)
    plt.axis('off')
    fig.tight_layout()
    fig.canvas.draw()
    # grab the pixel buffer and dump it into a numpy array
    pixels = np.array(fig.canvas.renderer._renderer)

    return pixels, data.values.ravel()

def img_show_shell(pixels):
    from PIL import Image
    im = Image.fromarray(pixels)
    im.show()

def img_plot_rgb(pixels):
    fig, ax = plt.subplots(1, 1) #,figsize=(10, 6))
    plt.axis('off')
    plt.imshow(pixels)
    return fig, ax

def test():

    X, y = img_gen_bar()
    img_plot_rgb(X)
    img_show_shell(X)
    print(y)

    #for neural net
    X=X/255

    #for DNN only
    #X=X.reshape(1,-1,3)
    return X, y

def test100():
    data={}
    for i in range(1000) :
        data[i] = (img_gen_bar() )

    return data

def img_gen(chttype='bar', batch = 10):
    if chttype=='rand':
        # use a random integer to choose the chart type
        chttype = np.random.randint(0,3)
        chttype = ['bar','plot','scatter','histo'][chttype]

    while True:
        X=[]
        y=[]
        for b in range(batch):
            if chttype=='bar':
                X_, y_ = img_gen_bar()
            else:
                #do something else
                X_, y_ = img_gen_bar()

            X.append(X_)
            y.append(y_.T)

        #Reshape image so we can stack them
#        h, w, d = np.shape(X_)
#        X = [x.reshape(1,h, w, d) for x in X] #, axis=0)

        X = np.array(X)
        y = np.stack(y)

        yield X, y
    return X, y
#matplotlib.rcParams['figure.figsize'] = (12.0, 8.0)


# In[Setup dat]:
# Then produce evaluation data set or 1 off dataset

gen = img_gen()
X, y = next(gen)
print("Shapes of 1st Training sets: ", np.shape(X), np.shape(y))

# In[Set backend]
#from keras import backend as K
#import importlib #requried for py3.5+


# In[Setup Config Variables]
verbose = 2  #True/False, extra verbose=2

config={}
#lagged return features
config['layers'] = 3  #Single Int
config['nodes'] = [34]#[75, 50, 25, 10, 5] #ignored for 1 layer. size of output. None to match X[0], Int or list of dims per layer. [10, 5, 1]
config['epochs'] = 10  # e.g. use 3*3 for 1st tstrain then 3 for tsroll (lower 'weight'? if less epochs)
config['actvn'] = 'tanh'  # sigmoid'

config['Description'] = 'Quintile Forecast'
config['StartTime'] = time.time()

config['Model'] = "CNN"


class_names = ['False','True']  # Corresponds to False, True

# In[helper functions]:
def plot_confusion_matrix(cm, classes,
                          normalize=False,
                          title='Confusion matrix',
                          cmap=plt.cm.Blues):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    """
    if normalize:
        #cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        cm = cm.astype('float') / np.sum(cm.ravel())
        print("Normalized confusion matrix")
    else:
        print('Confusion matrix, without normalization')

    print(cm)

    fig=plt.figure
    plt.imshow(cm, interpolation='nearest', cmap=cmap )
    plt.title(title)
    plt.colorbar()
    tick_marks = np.arange(len(classes))
    plt.xticks(tick_marks, classes, rotation=45)
    plt.yticks(tick_marks, classes)

    fmt = '.2f' if normalize else 'd'
    thresh = cm.max() / 2.
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        plt.text(j, i, format(cm[i, j], fmt),
                 horizontalalignment="center",
                 color="white" if cm[i, j] > thresh else "black")

    plt.tight_layout()
    plt.ylabel('True label')
    plt.xlabel('Predicted label')
    plt.show()
    return fig

def analyse_results(y_pred, y_true, model, bPrint=True):
    cnf_matrix = confusion_matrix(y_true, y_pred)
    fig=plot_confusion_matrix(cnf_matrix, classes=class_names, normalize=True, title=model+' Confusion Matrix')

    if bPrint:
        # Compute error between our test predictions and the actual values.
        print('MSE: %0.2f%%'%(mean_squared_error(y_pred, y_true)*100.0))
        # Compute the directional hit-rate
        print('Directional HitRate: %0.2f%%'%(sum(sum(cnf_matrix*np.eye(cnf_matrix.__len__())))/sum(cnf_matrix.ravel())*100.0))
    return fig

# In[]:
# Convolute timeseries into slices
def slice_mx(a, L):
    # time slice a matrix with L lookbacks.
    # See eg_slice.py
    s0,s1 = a.strides
    m,n   = a.shape
    return np.lib.stride_tricks.as_strided(a, shape=(m,n-L+1,L), strides=(s0,s1,s1))


def convolute(X, L, brotate=False, bcmpd=False):
    # lag matrix data [Dates x Features] and reshape into X=[Dates x Lags x Features]
    # RNN: 3D tensor with shape (batch_size, timesteps, input_dim).

    temp = [X.shift(i) for i in range(L)]
    temp =  np.dstack(temp)[L-1:,:,:]

    if bcmpd:
        temp[:,0,:] = np.cumprod(1+temp[:,0,:], axis=1)

    if brotate:
        temp =  np.rollaxis(temp,2,1)#[L:,:,:]

    return temp


def lag_and_rotate(ret, L):
    if np.ndim(ret)==1 or np.min(np.shape(ret))==1:
        # old school
        X = ret.to_frame().join(pd.DataFrame({ret.name + str(lag): ret.shift(lag) for lag in range(1,L)})).replace((np.inf,-np.inf),np.nan).fillna(0)
        X = X[L-1:]  #Drop incomplete look back window
    elif 'You really like pain'=='True':
        X = np.rot90(slice_mx(a=ret.as_matrix(), L=L), k=1)
        X = X[list(range(len(X)-1,-1,-1)),:,:]
    else: #2D > 3D rotate
        X=pd.DataFrame(index=ret.index)
        for name in ret.columns:
            temp = ret[name].to_frame().join(pd.DataFrame({name + str(lag): ret[name].shift(lag) for lag in range(1,L)}))
            X = X.join(temp)
            del temp

        X = X.replace((np.inf,-np.inf),np.nan).fillna(0)
        X = X[2*L-1:]  #Drop incomplete look back window

        X = X.as_matrix().reshape(-1,len(ret.columns),L)

        X = np.rot90(X,k=3, axes=(1,2))
        return X

# In[Build Keras Model]:


# In[Build Keras Model DNN]:
def model_DNN(x_train, y_train, x_test=None, y_test=None, kwargs={}):
    """ Build but do not run a NN model in Keras framework
        train and test data requried to determine model types, dimensions etc.
        x_train, y_train, x_test, y_test, kwargs = self.X, self.y, None, None, self.kwargs

        3D tensor with shape (batch_size, timesteps, input_dim).
    """
    ######## RELU??? DropOut
    # create and fit the LSTM network
    # input_shape = Lookback x Features

    #simpler loss recorder?    keras.callbacks.BaseLogger(stateful_metrics=None)

    from keras.optimizers import Adam, Nadam

    if kwargs.get('nodes', None) is None or kwargs.get('nodes', 0)==0 or kwargs.get('nodes', [0])==[0]:
        kwargs['nodes'] = [np.shape(x_train)[1]]
    elif isinstance(kwargs['nodes'] , (int, np.integer)): # turn int to list
        kwargs['nodes'] = [kwargs['nodes'] ]
    elif len(kwargs.get('nodes',[0])) < kwargs.get('layers', 1 ):
        kwargs['nodes'] = kwargs.get('nodes',[0])

    if kwargs.get('layers', 1 ) > 1 and len(kwargs.get('nodes')) < kwargs.get('layers',1):
        kwargs['nodes'] = list(np.pad(kwargs['nodes'] ,[0,kwargs.get('layers')-len(kwargs.get('nodes'))], mode='constant',constant_values=kwargs.get('nodes')[-1]))

    nodes = kwargs.get('nodes',[1])
    ndim = np.max([2,len(np.shape(x_train))]) # Min 2D
    if ndim==2:
        input_shape=(x_train.shape[1],)
    else:
        input_shape=(x_train.shape[1],x_train.shape[2])
    if np.ndim(y_train)==1:
        n_out = 1
    else:
        n_out = np.shape(y_train)[1] #e.g. onehot encoded.

    actvn = kwargs.get('actvn','tanh')
    if kwargs.get('onehot',False):
        actvl = kwargs.get('actvl','softmax')
    else:
        actvl = kwargs.get('actvl','tanh')

    if kwargs.get('bnorm', False):
        use_bias=False
    else:
        use_bias=True
    if kwargs.get('learning_rate', False):
        lr = kwargs.get('learning_rate')
    else:
        lr = False

    dropout = kwargs.get('dropout',False)


    model=[]
    model = Sequential()  # https://keras.io/models/sequential/                sess = tf.Session(config=tf.ConfigProto(log_device_placement=True))
    model.reset_states()
    if kwargs.get('layers',1)>1:
        for n in range(1,kwargs.get('layers')):
            if kwargs.get('verbose'): print('+adding extra layer')
            if kwargs.get('Model')=='AEP' and nodes[n-1]==np.min(nodes):
                # bottleneck!
                model.add(Dense(nodes[n-1], input_shape=input_shape, use_bias=use_bias,activation='linear', name="bottleneck"))  #https://www.cs.toronto.edu/~hinton/science.pdf
            else:
                model.add(Dense(nodes[n-1], input_shape=input_shape, use_bias=use_bias))  #kernel_initializer= http://proceedings.mlr.press/v9/glorot10a/glorot10a.pdf
                if kwargs.get('bnorm', False) : model.add(keras.layers.normalization.BatchNormalization())
                model.add(keras.layers.Activation(actvn))
            if dropout:
                model.add(Dropout(dropout)) #(c.f. Regularisation of Betas)


        # Add last output layer with 1 node for y
        if ndim>2:
            model.add(Flatten())
            model.add(Dense(nodes[n]*nodes[n], actvn))

    model.add(Dense(n_out, input_shape=input_shape, activation=actvl))

    #Fun Hack >> Require re/compile to become effective!
    if kwargs.get('freeze', False) and 'start_weights' in kwargs.keys():
        for layer in model.layers:
            layer.trainable = False


    #defaults = keras.optimizers.Adam(lr=0.001, beta_1=0.9, beta_2=0.999, epsilon=None, decay=0.0, amsgrad=False)
    if hasattr(kwargs,'optimizer'):
        optimizer = kwargs['optimizer']
    elif lr:
        #optimizer = Adam(lr=lr, beta_1=0.875, beta_2=0.95, epsilon=1e-8, decay=0.01)# laterversion>>, amsgrad=False)
        optimizer = Nadam(lr=lr, beta_1=0.9, beta_2=0.999, epsilon=1e-8, schedule_decay=0.004)

    else:
        #fails optimizer = keras.optimizers.Adam(lr=0.001, beta_1=0.9, beta_2=0.999, epsilon=1e-8, decay=0)# laterversion>>, amsgrad=False)
        optimizer = 'adam'
        optimizer = Nadam(lr=0.005, beta_1=0.9, beta_2=0.990, epsilon=1e-8, schedule_decay=0.004)


    #    lr_metric = get_lr_metric(optimizer)
    #    from sklearn.metrics import r2_score glorot_normal

    if is_bool_dtype(y_train):
        model.compile(loss='binary_crossentropy', optimizer=optimizer, metrics=['accuracy']) # , lr_metric
    if is_categorical_dtype(y_train) or kwargs.get('onehot',False):
        # Multiple Category
        model.compile(loss='categorical_crossentropy', optimizer=optimizer, metrics=['accuracy'])
    else:
        model.compile(loss='mean_squared_error', optimizer=optimizer, metrics=[r2_keras])

    if kwargs.get('verbose',False) > 1:
        model.summary()
        print("Inputs: {}".format(model.input_shape))
        print("Outputs: {}".format(model.output_shape))
        print("Actual input: {}".format(x_train.shape))
        print("Actual output: {}".format(y_train.shape))
        print('Model Loss: ' + model.loss)

    # For compatability with other models;
    model.score = model.evaluate

    return model #self.model=model


# In[Build Keras Model LSTM/RNN]:
def model_RNN(x_train, y_train, x_test=None, y_test=None, kwargs={}):
    """ Build but do not run a NN model in Keras framework
        train and test data requried to determine model types, dimensions etc.
        x_train, y_train, x_test, y_test, kwargs = self.X3, self.y, None, None, self.kwargs
    """
    """
    Notes on Input shape
    3D tensor with shape (batch_size, timesteps, input_dim).
    https://keras.io/layers/recurrent/
    LSTMs in Keras are typically used on 3d data (batch dimension, timesteps, features).
    LSTM without return_sequences will output (batch dimension, output features)
    LSTM with return_sequences will output (batch dimension, timesteps, output features)
    Basic timeseries data has an input shape (number of sequences, steps, features). Target is (number of sequences, steps, targets). Use an LSTM with return_sequences.
    """
    ######## RELU??? DropOut
    # create and fit the LSTM network
    # input_shape = Lookback x Features
    verbose = kwargs.get('verbose',False)
    layers  = kwargs.get('layers', 1 )
    nodes   = kwargs.get('nodes', None)

    if nodes is None or nodes==0 or nodes==[0]:
        nodes = [np.shape(x_train)[1]]
    elif isinstance(nodes, (int, np.integer)): # turn int to list
        nodes = [nodes]

    if layers > 1 and len(nodes) < layers:
        nodes = list(np.pad(nodes,[0,layers-len(nodes)], mode='constant',constant_values=nodes[-1]))

    ndim = np.max([2,len(np.shape(x_train))]) # Min 2D
    if ndim==2:
        input_shape=(x_train.shape[1],)
    else:
        input_shape=(x_train.shape[1],x_train.shape[2])
    if kwargs.get('learning_rate', False):
        lr = kwargs.get('learning_rate')
    else:
        lr = False

    if np.ndim(y_train)==1:
        n_out = 1 #e.g. forecast y as float, just 1 step ahead.
    else:
        n_out = np.shape(y_train)[1] #e.g. onehot encoded, or n-steps ahead.

    dropout = kwargs.get('dropout',0) # dropout rate between 0 and 1.
    stateful = kwargs.get('stateful',True)
    if stateful: #RNN needs fixed batch - consider using static_index
        batch_shape = (kwargs.get('batch_size',1234),)  + input_shape
    actvn = kwargs.get('actvn','tanh')
    actvl = kwargs.get('actvl','sigmoid')
    if verbose and not actvn == 'tanh': print('tanh activation recommended for LSTM but you are using',actvn)

    model=[]
    model = Sequential()  # https://keras.io/models/sequential/
    model.reset_states() # ?useful for batch training RNN... perhaps inside batched loop
    #TODO? model.add(Embedding(max_features, output_dim=n_out))

    if layers>1:
        for n in range(1,layers):
            if kwargs.get('verbose'): print('+adding extra layer')
            if stateful: #switch between batch_ and input_shape
                model.add(LSTM(nodes[layers-1], batch_input_shape=batch_shape, return_sequences=True, activation=actvn, stateful=stateful))
            else:
                model.add(LSTM(nodes[layers-1], input_shape=input_shape, return_sequences=True, activation=actvn, stateful=stateful))
            if kwargs.get('bnorm', False):
                model.add(keras.layers.normalization.BatchNormalization())
            # TODO find out about time lock dropout
            if dropout:
                model.add(Dropout(dropout)) #(c.f. Regularisation of Betas)

    # Single layer or last layer of RNN
    if stateful:
        model.add(LSTM(nodes[layers-1], batch_input_shape=batch_shape, return_sequences=False, activation=actvn, stateful=stateful))
    else:
        model.add(LSTM(nodes[layers-1], input_shape=input_shape, return_sequences=False, activation=actvn, stateful=stateful))

    #model.add(Flatten()) # Req'd if last layer return_sequences=True
    #model.add(Dense(nodes[layers-1]**2, activation=actvl))
    model.add(Dense(n_out, activation=actvl))

    #defaults = keras.optimizers.Nadam(lr=0.002, beta_1=0.9, beta_2=0.999, epsilon=None, schedule_decay=0.004)
    if hasattr(kwargs,'optimizer'):
        optimizer = kwargs['optimizer']
    elif lr:
        optimizer = keras.optimizers.Nadam(lr=lr, beta_1=0.9, beta_2=0.999, epsilon=1e-8, schedule_decay=0.004)
    else:
        optimizer = keras.optimizers.Nadam(lr=0.002, beta_1=0.9, beta_2=0.999, epsilon=1e-8,schedule_decay=0.004)
        optimizer = 'adam'

    if is_bool_dtype(y_train):
        model.compile(loss='binary_crossentropy', optimizer=optimizer)
    if is_categorical_dtype(y_train) or kwargs.get('onehot',False):
        #TODO Multiple Category
        model.compile(loss='categorical_crossentropy', optimizer=optimizer)
    else:
        model.compile(loss='mean_squared_error', optimizer=optimizer)

    if verbose  > 1:
        model.summary()
        print("Inputs: {}".format(model.input_shape))
        print("Outputs: {}".format(model.output_shape))
        print("Actual input: {}".format(x_train.shape))
        print("Actual output: {}".format(y_train.shape))
        print('Model Loss: ' + model.loss)

    # For compatability with other models;
    model.score = model.evaluate

    return model #self.model=model


# In[Build Keras Model CNN]:
def model_CNN(x_train, y_train, x_test=None, y_test=None, kwargs={}):
    """ Build but do not run a NN model in Keras framework
        train and test data requried to determine model types, dimensions etc.
        x_train, y_train, x_test, y_test, kwargs = self.X4, self.y, None, None, self.kwargs
    """
    """
    Notes on Input shape
    4D tensor with shape (batch_size, timesteps, features, `colors`).
    4D tensor with shape: (samples, rows, cols, channels)
    `channels_last` (default)
    Output 4D tensor with shape: (samples, new_rows, new_cols, filters)
    """
    ######## CNN for stocks
    # create and fit CNN
    # input_shape = StockDate x Lookback x Features
    from keras.layers import Conv2D, MaxPooling2D
    from keras.optimizers import SGD


    layers = kwargs.get('layers', 1 ) #TODO
    nodes = kwargs.get('nodes', None) #TODO

    if nodes is None or nodes==0 or nodes==[0]:
        nodes = [np.shape(x_train)[1]]
    elif isinstance(nodes, (int, np.integer)): # turn int to list
        nodes = [nodes]

    if layers > 1 and len(nodes) < layers:
        nodes = list(np.pad(nodes,[0,layers-len(nodes)], mode='constant',constant_values=nodes[-1]))

    ndim = np.max([2,len(np.shape(x_train))]) # Min 2D
    if ndim==2:
        input_shape=(x_train.shape[1],)
    elif ndim==3:
        input_shape=(x_train.shape[1],x_train.shape[2])
    elif ndim==4:
        input_shape=(x_train.shape[1],x_train.shape[2],x_train.shape[3])
    else:
        input_shape=x_train.shape[1:]
    if kwargs.get('learning_rate', False):
        lr = kwargs.get('learning_rate')
    else:
        lr = False

    if False:
        conv = (3, 3)
    else:
        conv = (2, 2)
        n_conv = 5

    if np.ndim(y_train)==1:
        n_out = 1 #e.g. forecast y as float, just 1 step ahead.
    else:
        n_out = np.shape(y_train)[1] #e.g. onehot encoded, or n-steps ahead.

    dropout = kwargs.get('dropout',0) # dropout rate between 0 and 1.
    #stateful = kwargs.get('stateful',True)
    actvn = 'relu' #kwargs.get('actvn','relu')
    actvl = kwargs.get('actvl','sigmoid')
    model=[]
    model = Sequential()  # https://keras.io/models/sequential/
    model.reset_states()
    # input: 100x100 images with 3 channels -> (100, 100, 3) tensors.
    # this applies 32 convolution filters of size 3x3 each.
    model.add(Conv2D(n_conv, conv, activation=actvn, input_shape=input_shape))
    model.add(Conv2D(n_conv, conv, activation=actvn))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(dropout ))

    model.add(Conv2D(n_conv*2, conv, activation=actvn))
    model.add(Conv2D(n_conv*2, conv, activation=actvn))
    #model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(dropout ))

    model.add(Flatten())
    model.add(Dense(np.max(input_shape), activation=actvn))
    model.add(Dropout(dropout*2))
    model.add(Dense(n_out, activation=actvl))

    if hasattr(kwargs,'optimizer'):
        optimizer = kwargs['optimizer']
    elif lr:
        optimizer = SGD(lr=lr, decay=1e-6, momentum=0.01, nesterov=True)
    else:
        optimizer = keras.optimizers.SGD(lr=0.01, momentum=0.0, decay=0.0, nesterov=False)

    if is_bool_dtype(y_train):
        model.compile(loss='binary_crossentropy', optimizer=optimizer)
    if is_categorical_dtype(y_train) or kwargs.get('onehot',False):
        #TODO Multiple Category
        model.compile(loss='categorical_crossentropy', optimizer=optimizer)
    else:
        #model.compile(loss='mean_squared_error', optimizer=optimizer)
        model.compile(loss='mean_squared_error', optimizer=optimizer, metrics=[r2_keras])


    if kwargs.get('verbose',False) > 1:
        model.summary()
        print("Inputs: {}".format(model.input_shape))
        print("Outputs: {}".format(model.output_shape))
        print("Actual input: {}".format(x_train.shape))
        print("Actual output: {}".format(y_train.shape))
        print('Model Loss: ' + model.loss)

    # For compatability with other models;
    model.score = model.evaluate

    return model #self.model=model


def r2_keras(y_true, y_pred):
    SS_res =  K.sum(K.square(y_true - y_pred))
    SS_tot = K.sum(K.square(y_true - K.mean(y_true)))
    #( 1 - SS_res/(SS_tot + K.epsilon()) )
    return ( 1 - SS_res/(SS_tot ) )


# In[Build Keras Model ]:

model = model_CNN(X, y, x_test=None, y_test=None, kwargs=config)
print(model.summary())
print("Inputs: {}".format(model.input_shape))
print("Outputs: {}".format(model.output_shape))
print("Actual input: {}".format(X.shape))
print("Actual output: {}".format(y.shape))
print('Model Loss: ' + model.loss)

# In[Evaluate/Test Keras Simple]:
if True:
    if True:
        #model.evaluate(x_train_k, y_train_k, batch_size=1, verbose=1)
        mdlfit=[]
        mdlscr=[]
        mdlprd=[]

        # Make predictions.
        if True: #Use generator
            i = 0
            while i < getattr(config, 'iterations' , 3333):
                i += 1
                X, y = next(gen)
                if verbose: print('Training: Fit and Predicting for %s. Shapes: '%('img'), np.shape(X), np.shape(y))

                # Step 1
                m = model.fit_generator(gen, steps_per_epoch=100, verbose=1)
                mdlfit.append(m) #, steps=100))
                s = model.evaluate_generator(gen, steps=10)
                mdlscr.append(s)
                # evaluate the model

                #if verbose>1:print("%s: %.2f%%" % (model.metrics_names[0], scores[-1]*100))

            # Finished let's see
            X_test,  y_test  = next(gen)

            mdlprd = model.predict(X_test)   #, index=X.loc[x_train.index[L:]].index)

        else: # Single estimate
            X_train,  y_train  = next(gen)
            X_test,  y_test  = next(gen)
            mdlfit = model.fit(X_train, y_train, epochs=config.get('epochs'), batch_size=1, verbose=1, shuffle=False) #y.astype(int)
            mdlprd = model.predict(X_test)   #, index=X.loc[x_train.index[L:]].index)
            mdlscr = model.score(X_test, y_test)

# In[Save Keras Model]:

hist = pd.DataFrame([m.history['loss']  for m in mdlfit],columns=['loss'])
print('training loss',hist)
print('oos loss')
#hist.plot()
# In[Save Keras Model]:
#    strNN = 'LSTM' if bLSM else 'DNN'
#    fname = '%s %s %i lags %i x %i %s layers %i epochs with %i dims & %s activation'%\
#            (K.backend(), config.get('Description'),
#             config.get('tsroll') if config.get('tsroll') else L,
#             config.get('nodes')[0], config.get('layers'), strNN,
#             config.get('epochs'), ndim, config.get('actvn'))
#    model.save(fname + '.hdf')
#    if verbose>=1:
#        print('Saved model to;', fname+'.hdf')
#    wts = model.get_weights()



#    if True:
#            #Wt History
##            weights.values.reshape(np.len(weights.index), np.sqrt(len(weights.columns)),-1)
#
#        print('MSE: %0.2f%% using; %i lags, %i nodes, %i layers and %i epochs, with shuffle? %r and rolled? %r. '%(mean_squared_error(y_pred2, y_actu2)*100.0, L, config.get('nodes')[0], config.get('layers'), config.get('epochs'), config.get('bshuffle'), config.get('brotate')))
#        #print('Training Correl: %0.1f%% Test Correl: %0.1f%%.'%(100.0*np.corrcoef(y_pred,y_actu)[1][0],100.0*np.corrcoef(y_pred2, y_actu2)[1][0]))
#        print('Test Correl: %0.1f%%.'%(100.0*np.corrcoef(y_pred2, y_actu2)[1][0]))

#        summary=pd.concat([actuals_train.stack().describe(),predict_train.stack().describe(),actuals_test.stack().describe(),predict_test.stack().describe(),], axis=1)
#        summary.columns=['InSample','Train','OOS','Test']
#        print(summary)
