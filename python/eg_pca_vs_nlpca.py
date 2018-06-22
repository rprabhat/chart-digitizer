# -*- coding: utf-8 -*-
"""
Created on Tue Mar  6 10:43:44 2018
TODO: http://www.nlpca.org/ Here, NLPCA is applied to 19-dimensional spectral data representing equivalent widths of 19 absorption lines of 487 stars, available at www.cida.ve. The figure in the middle shows a visualisation of the data by using the first three components of standard PCA. Data of different colors belong to different spectral groups of stars. The first three components of linear PCA and of NLPCA are represented by grids in the left and right figure, respectively. Each grid represents the two-dimensional subspace given by two components while the third one is set to zero. Thus, the grids represent the new coordinate system of the transformation. In contrast to linear PCA (left) which does not describe the nonlinear characteristics of the data, NLPCA gives a nonlinear (curved) description of the data, shown on the right.
see https://bitbucket.org/matthias-scholz/nonlinear-pca-toolbox-for-matlab


@author: i015225
"""

import pylab as plt
import numpy as np
import seaborn as sns; sns.set()

from sklearn.decomposition import PCA
import keras
from keras.datasets import mnist
from keras.models import Sequential, Model
from keras.layers import Dense
from keras.optimizers import Adam

import gzip
import sys
import pickle

#@In[basic iris example]
# https://stats.stackexchange.com/questions/229092/how-to-reverse-pca-and-reconstruct-original-variables-from-several-principal-com/229093
#
#import numpy as np
#import sklearn.datasets, sklearn.decomposition
#
#X = sklearn.datasets.load_iris().data
#mu = np.mean(X, axis=0)
#
#pca = sklearn.decomposition.PCA()
#pca.fit(X)
#
#n_comp = 3 #2 or 3 = 2
#Xhat = np.dot(pca.transform(X)[:,:n_comp], pca.components_[:n_comp,:])
#Xhat += mu
#
#print(Xhat[0,])
#print(X[0,])


# In[setup MINST data]
# https://stats.stackexchange.com/questions/190148/building-an-autoencoder-in-tensorflow-to-surpass-pca

# if online
#(x_train, y_train), (x_test, y_test) = mnist.load_data()
# else

f = gzip.open('C:\JPQ\Python\Example\mnist.pkl.gz', 'rb')
if sys.version_info < (3,):
    data = pickle.load(f)
else:
    data = pickle.load(f, encoding='bytes')
f.close()

(x_train, y_train), (x_test, y_test) = data

x_train = x_train.reshape(60000, 784) / 255
x_test = x_test.reshape(10000, 784) / 255

# In[Run PCA vs AE]
n_comp = 2 #2 or 3 = 2

# PCA (Classic )

mu = x_train.mean(axis=0)
pca = PCA()
pca.fit(x_train)

# X-hat
Zpca = pca.transform(x_train)# [:,:n_comp] #np.dot(pca.transform(x_train)[:,:n_comp], pca.components_[:n_comp,:])
Rpca = np.dot(Zpca[:,:n_comp], pca.components_[:n_comp,:]) + mu # reconstruction

errpca = np.sum((x_train-Rpca)**2)/Rpca.shape[0]/Rpca.shape[1]
print('PCA reconstruction error with %i PCs: %0.3f'%(n_comp,errpca))
#This outputs: PCA SVD reconstruction error with 2 PCs: 0.056

# PCA (SVD = Single Value Decomposition)

mu = x_train.mean(axis=0)
U,s,V = np.linalg.svd(x_train - mu, full_matrices=False)
Zsvd = np.dot(x_train - mu, V.transpose())

Rsvd = np.dot(Zsvd[:,:2], V[:2,:]) + mu    # reconstruction
errsvd = np.sum((x_train-Rsvd)**2)/Rsvd.shape[0]/Rsvd.shape[1]
print('PCA reconstruction error with %i PCs: %0.3f'%(n_comp,errsvd))
#This outputs: PCA SVD reconstruction error with 2 PCs: 0.056

# Training the autoencoder
# 28x28 pixels = 784 vector >> 512 >> 128 >> 2 << 128 << 512 << 784
# n_comp = 3 #2 or 3

m = Sequential()
m.add(Dense(512,  activation='elu', input_shape=(784,)))
m.add(Dense(128,  activation='elu'))
m.add(Dense(n_comp ,    activation='linear', name="bottleneck"))  # was 2
m.add(Dense(128,  activation='elu'))
m.add(Dense(512,  activation='elu'))
m.add(Dense(784,  activation='sigmoid'))
m.compile(loss='mean_squared_error', optimizer = Adam())

history = m.fit(x_train, x_train, batch_size=128, epochs=5, verbose=1, 
                validation_data=(x_test, x_test))


encoder = Model(m.input, m.get_layer('bottleneck').output)
Zenc = encoder.predict(x_train)  # bottleneck representation
Renc = m.predict(x_train)        # reconstruction


errnlpca = np.sum((x_train-Renc)**2)/Renc.shape[0]/Renc.shape[1]
print('PCA reconstruction error with %i PCs: %0.3f'%(n_comp,errnlpca))

# Wts
w = encoder.get_weights()
#wt = encoder.weights #c.f. encoder.summary()

# In[Plots]
# Plotting PCA projection side-by-side with the bottleneck representation
nplot = 1000  # nplot
if n_comp == 3:
    #https://matplotlib.org/mpl_toolkits/mplot3d/tutorial.html
    
    from mpl_toolkits.mplot3d import Axes3D

    fig = plt.figure(figsize=(16,8))
    ax = fig.add_subplot(121, projection='3d')
    plt.title('PCA')
    ax.scatter(Zpca[:nplot,0], Zpca[:nplot,1], Zpca[:nplot,2], c=y_train[:nplot], s=8, cmap='tab10')
    ax.get_xaxis().set_ticklabels([])
    ax.get_yaxis().set_ticklabels([])
    ax.set_zticklabels([])
    
    ax = fig.add_subplot(122, projection='3d')
    plt.title('Autoencoder')
    ax.scatter(Zenc[:nplot,0], Zenc[:nplot,1], Zenc[:nplot,2], c=y_train[:nplot], s=8, cmap='tab10')
    ax.get_xaxis().set_ticklabels([])
    ax.get_yaxis().set_ticklabels([])
    ax.set_zticklabels([])
    
    plt.tight_layout()
    
#    fig = plt.figure(figsize=(16,8))
#    ax = fig.add_subplot(121, projection='3d')
#    plt.title('PCA SVD')
#    ax.scatter(Zsvd[:nplot,0], Zsvd[:nplot,1], Zsvd[:nplot,2], c=y_train[:nplot], s=8, cmap='tab10')
#    ax.get_xaxis().set_ticklabels([])
#    ax.get_yaxis().set_ticklabels([])
#    ax.set_zticklabels([])
#    
#    ax = fig.add_subplot(122, projection='3d')
#    plt.title('Autoencoder')
#    ax.scatter(Zenc[:nplot,0], Zenc[:nplot,1], Zenc[:nplot,2], c=y_train[:nplot], s=8, cmap='tab10')
#    ax.get_xaxis().set_ticklabels([])
#    ax.get_yaxis().set_ticklabels([])
#    ax.set_zticklabels([])
#    
#    plt.tight_layout()    

elif n_comp == 2:
    plt.figure(figsize=(8,4))
    plt.subplot(121)
    plt.title('PCA')
    plt.scatter(Zpca[:nplot,0], Zpca[:nplot,1], c=y_train[:nplot], s=8, cmap='tab10')
    plt.gca().get_xaxis().set_ticklabels([])
    plt.gca().get_yaxis().set_ticklabels([])
    
    plt.subplot(122)
    plt.title('Autoencoder')
    plt.scatter(Zenc[:nplot,0], Zenc[:nplot,1], c=y_train[:nplot], s=8, cmap='tab10')
    plt.gca().get_xaxis().set_ticklabels([])
    plt.gca().get_yaxis().set_ticklabels([])
    
    plt.tight_layout()
    
#    plt.figure(figsize=(8,4))
#    plt.subplot(121)
#    plt.title('PCA SVD')
#    plt.scatter(Zsvd[:nplot,0], Zsvd[:nplot,1], c=y_train[:nplot], s=8, cmap='tab10')
#    plt.gca().get_xaxis().set_ticklabels([])
#    plt.gca().get_yaxis().set_ticklabels([])
#    
#    plt.subplot(122)
#    plt.title('Autoencoder')
#    plt.scatter(Zenc[:nplot,0], Zenc[:nplot,1], c=y_train[:nplot], s=8, cmap='tab10')
#    plt.gca().get_xaxis().set_ticklabels([])
#    plt.gca().get_yaxis().set_ticklabels([])
#    
#    plt.tight_layout()
    
    


# In[Reconstructions]
# And now let's look at the reconstructions (first row - original images, second row - PCA, third row - autoencoder):

toPlot = (x_train, Rpca, Rsvd, Renc)
plt.figure(figsize=(9,len(toPlot)))
for i in range(10):
    for j in range(len(toPlot)):
        ax = plt.subplot(len(toPlot), 10, 10*j+i+1)
        plt.imshow(toPlot[j][i,:].reshape(28,28), interpolation="nearest", 
                   vmin=0, vmax=1)
        plt.gray()
        ax.get_xaxis().set_visible(False)
        ax.get_yaxis().set_visible(False)

plt.tight_layout()