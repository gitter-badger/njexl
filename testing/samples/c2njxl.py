__author__ = 'noga'
'''
This is a template python file,
for converting a CHAITIN file to nJexl format
'''


def process_and_convert(line,o_file):
    words = line.split("\t")
    command = words[0].strip()
    if command.t ==

    if command.contains("selenium."):
        return
    command.split()


    pass


def parse_and_convert_file(chaitin_file_name, jxl_file_name):
    file = open(chaitin_file_name)
    o_file=open(jxl_file_name,'w')
    for line in file:
        process_and_convert(line,o_file)
    pass
