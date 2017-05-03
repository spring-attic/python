__author__ = 'David Turanski'
import sys
import os

sys.path.append(os.path.abspath('src/main/resources/python'))
from springcloudstream.stream import Processor

def echo(data):
    return data

processor =  Processor()
processor.start(echo)
