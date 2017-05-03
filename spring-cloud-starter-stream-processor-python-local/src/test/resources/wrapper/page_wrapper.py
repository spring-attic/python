import cPickle as pickle
import json
from java.lang import String

'''
Map to a Python object
'''


class Page:
    def __init__(self, page):
        self.links = {}
        for key in page.links:
            self.links[key] = page.links[key].toString()
        self.images = {}
        for key in page.images:
            self.images[key] = page.images[key]


# protocol = pickle.HIGHEST_PROTOCOL
protocol = 1

'payload is bound to Java object spring.io.data.Page'
page = Page(payload)

'Pickle the Page dict representation'
input = pickle.dumps(page.__dict__, protocol)
'processor is bound to ShellProcessor. Invoke the shell processor and receive a dict'
data = processor.sendAndReceive(input)
'deserialize'
returned_page = pickle.loads(data)
'transform to java String as Json'
result = String(json.dumps(returned_page))
