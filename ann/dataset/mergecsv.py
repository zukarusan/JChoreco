import pandas
import csv
import glob

dir = './subset/'
columns = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "time", 'chord']
names = glob.glob(dir+'CRP*.csv')
with open('_MERGED_.csv', 'w') as fout:
    i = 0
    total = len(names)
    for filename in names:
        # drop 7th chord, comment to include
        if '7' in filename:
            i+=1
            continue
        print("Merging... %d%%" % (i*100//total), end='\r')
        with open(filename, 'r') as fin:
            for line in fin:
                fout.write(line)
        i += 1

print('Merged 100%% complete.')
merged = pandas.read_csv('_MERGED_.csv')
merged.columns = columns
merged.to_csv('_MERGED-HEADER_.csv', index=False)
shuffled = merged.sample(frac=1)
shuffled.to_csv('_SHUFFLED_.csv', index=False, header=None)
shuffled.to_csv('_SHUFFLED-HEADER_.csv', index=False)
