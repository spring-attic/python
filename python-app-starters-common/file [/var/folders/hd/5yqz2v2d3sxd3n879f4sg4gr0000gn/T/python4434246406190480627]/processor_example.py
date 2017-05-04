from springcloudstream.stream import Processor
__author__ = 'David Turanski'


def echo(data):
    return data


process = Processor()
process.start(echo)