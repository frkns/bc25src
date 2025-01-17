# the format should look like this:
# /*tune:1*/500/**/
# wrap the paramter to be tuned in /*tune:{x}*/567/**/, where x is unique

# to tune 500 -> 550 for example,
# sed outline:  s#/*tune:1*/500/**/#/*tune:1*/550/**/#g  (using # as a delimiter)
# the actual command needs to escape *
# sed -i 's#/\*tune:1\*/500/\*\*/#/\*tune:1\*/550/\*\*/#g' filename

# instead using sed, can also use autotunerCopy

#   key: int,   tune ID (tune:ID)
# value: Tuple, (lower_bound, upper_bound, step)
gen_list = {

}