from springcloudstream.stream import Processor

__author__ = 'David Turanski'

import sys

def upper(data):
    return data.upper()

Processor(upper,sys.argv).start()

