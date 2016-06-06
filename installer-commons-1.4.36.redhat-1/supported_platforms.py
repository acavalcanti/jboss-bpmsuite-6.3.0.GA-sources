# Supported Platforms Combinator: Parses the Installer product layering matrix 
# found at https://mojo.redhat.com/docs/DOC-179024 to produce a single
# binary word string that represents whether certain combinations of
# products are valid or not.
#
# The scheme is as follows:
# The mojo document contains a table showing allowed product combinations:
# ie. EAP, EAP + SOA, EAP + SOA + EDS. 
# These three combinations represent allowed platform installations.
# This script will transform each allowed combination into a single binary word
# of p bits, where p is the number of products.
# 
# These combo words will be used to produce a single binary word of 2 ^ p bits.
# The ith bit in this word represents the validity of the product combination 
# given by the binary word for i.
# ie. The combination EAP + SOA + EDS may be represented by the binary word
# 100011 = 35.
# If the 35th digit in the validity word is 1, this combination is valid. 
# Otherwise, it's not.
import argparse
import re

EAP = "EAP"
SOA = "SOA"
SRAMP="S-RAMP"
DV = "EDS"
BPMS = "BPMS"
BRMS = "BRMS"

TRUE = "1"
FALSE = "0"

# Order is important, since the installer's layer's validator uses
# this order in its Truth Table
PRODS = [BRMS, BPMS, SRAMP, DV, SOA]

def parse_matrix(filename="layers.txt"):
	"""	Extract all of the valid layer combination strings from the layers/PRODS
	matrix file. """
	layers_list = []
	with open(filename, 'r') as f:
		for line in f:
			layers = extract_layers(line)
			if layers:
				layers_list.append(layers)
	return layers_list
	

def extract_layers(line):
	"""Given a single line from the layers matrix, extract a layers
	combination string of the form: prod1 + prod2 + ...
	ie. EAP + SOA

	Returns None if no such string was found."""
	# match one or more repetitions of the pattern:
	# PROD + OTHERPROD
	pattern = "((.*)\+(.*))+"
	cols = line.split('\t')
	match = re.search(pattern, cols[0])
	if match:
		return cols[0]
	else:
		return None

def transform_layers_to_binary_word(layers):
	"""	Given a single string with a layers combination, produce its
	equivalent binary word.
	ie: SOA + DV -> 00011 """
	word = ""
	for layer in PRODS:
		if layer in layers:
			word = word + TRUE
		else:
			word = word + FALSE
	return word

def text_to_binary_words(list_of_combos):
	"""Given a list of layer combination strings, produce a list
	of binary words representing these combinations."""
	words = []
	for layers in list_of_combos:
		words.append(transform_layers_to_binary_word(layers))
	return words

def words_to_int_sequence(words_list):
	""" Given a list of binary words, returns a list of equivalent
	integers in non-decreasing order."""
	return sorted(map(binary_to_integer,words_list))

def binary_to_integer(word):
	""" Converts binary string to integer """
	return int(word,2)
	
def create_validator_string(int_sequence):
	""" Given a sequence of integers, produces a binary word representing
	the output values of a truth table of 2 ^ n rows, where n is the
	number of bits needed to represent the largest integer in binary
	form. The output value is TRUE if the integer is part of the
	given sequence, FALSE otherwise."""
	val = ""
	# Number of combinations possible with n products: 2 to the n
	rows = 2 ** len(PRODS) 
	index = 0

	# Do a single pass through list of possible combinations to find
	# whether said combination is valid.
	for i in range(rows):
		if i == int_sequence[index]:
			val = val + TRUE
			if (index < len(int_sequence) - 1 ):
				index += 1
		else:
			val = val + FALSE
	return val

def report(layers, words, val_string):
	""" Print some stuffs."""
	print str(len(layers)) + " valid combinations found out of a possible" + str(2**len(PRODS)) + " possible number of combinations."
	print "Validation String:"
	print val_string

if __name__=="__main__":
 	parser = argparse.ArgumentParser(
            description="Parses the Installer's Product Layering Matrix \
            and Outputs a binary word string representing validity of all \
            possible layer combinations.")
 	parser.add_argument("layers_file",
 		help ="The file containing the Product Layering Matrix in text format.")

 	args=parser.parse_args()
 	layers_list = parse_matrix(args.layers_file)
 	words_list = text_to_binary_words(layers_list)
 	val_sequence =  words_to_int_sequence(words_list)
 	val_string = create_validator_string(val_sequence)
 	report(layers_list, words_list, val_string)

