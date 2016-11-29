grams = "5_gram"
results = open("results_retrofit_5_gram.txt", 'r').readlines()
score_1 = open("score_1"+grams+".txt", 'w+')
score_2 = open("score_2"+grams+".txt", 'w+')
score_3 = open("score_3"+grams+".txt", 'w+')
score_4 = open("score_4"+grams+".txt", 'w+')
score_5 = open("score_5"+grams+".txt", 'w+')

sc_1_total = 0
sc_2_total = 0
sc_3_total = 0
sc_4_total = 0
sc_5_total = 0
tot_files = 0
for i in range(0,len(results), 7):
	if "NaN" in results[i+2]:
		continue

	tot_files +=1

	sc_1 = results[i+2].split(": ")
	score_1.write(sc_1[-1])
	sc_1_total += float(sc_1[-1].strip())

	sc_2 = results[i+3].split(": ")
	score_2.write(sc_2[-1])
	sc_2_total += float(sc_2[-1].strip())

	sc_3 = results[i+4].split(": ")
	score_3.write(sc_3[-1])
	sc_3_total += float(sc_3[-1].strip())

	sc_4 = results[i+5].split(": ")
	score_4.write(sc_4[-1])
	sc_4_total += float(sc_4[-1].strip())

	sc_5 = results[i+6].split(": ")
	score_5.write(sc_5[-1])
	sc_5_total += float(sc_5[-1].strip())

score_1.write("Avg: "+str(sc_1_total/tot_files)+"\n")
score_1.close()
score_2.write("Avg: "+str(sc_2_total/tot_files)+"\n")
score_2.close()
score_3.write("Avg: "+str(sc_3_total/tot_files)+"\n")
score_3.close()
score_4.write("Avg: "+str(sc_4_total/tot_files)+"\n")
score_4.close()
score_5.write("Avg: "+str(sc_5_total/tot_files)+"\n")
score_5.close()