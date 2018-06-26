# -*- coding: utf-8 -*-
"""
Created on Tue Jun 26 15:04:00 2018

@author: i015225
"""


import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
#import seaborn as sns
#import io
#from PIL import Image


def img_gen_bar():
    """ Generate a random bar chart exporting height array and pixel array
        TODO:
        TODO: Add options for randomising scale, # bars, showing axis, grids, rotating labels, legends etc.
    """
    data = pd.DataFrame(data=np.random.rand(5,1), index=range(1,6), columns=['Fred'])
    m,n = np.shape(data)

    plt.bar(x=data.index.values,height=data.values.ravel(), color='k')
    plt.axis('off')
    fig=plt.gcf()
    fig.canvas.draw()
    # grab the pixel buffer and dump it into a numpy array
    pixels = np.array(fig.canvas.renderer._renderer)

    return data.values.ravel() , pixels


def img_plot_rgb(pixels):
    fig, ax = plt.subplots(1, 1)
    plt.axis('off')
    plt.imshow(pixels)
    return fig, ax


y, X = img_gen_bar()
img_plot_rgb(X)
print(y)

#for neural net
X=X/255

#for DNN only
#X=X.reshape(1,-1,3)

data={}
for i in range(1000) :
    data[i] = (img_gen_bar() )
