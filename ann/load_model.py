print('Setting up.', end='\r')
import os
from tensorflow.keras import layers, Model, Input, Sequential, models
print('Setting up..', end='\r')
import pandas as pd
# from matplotlib import pyplot a   s plotter
print('Setting up...', end='\r')

MODEL_DIR = os.path.join('.', 'saved_model')
MODEL_NAMES = os.listdir(MODEL_DIR)
PRED_COLS = ['A#maj', 'A#min', 'Amaj', 'Amin', 'Bmaj', 'Bmin', 'C#maj', 
            'C#min', 'Cmaj', 'Cmin', 'D#maj', 'D#min', 'Dmaj', 'Dmin', 
            'Emaj', 'Emin', 'F#maj', 'F#min', 'Fmaj', 'Fmin', 'G#maj', 'G#min', 'Gmaj', 'Gmin']
loaded = []
print('Setup complete.')
for mn in MODEL_NAMES:
    loaded.append(models.load_model(os.path.join(MODEL_DIR, mn)))
print('\n\nTotal models:', len(MODEL_NAMES), '\nModels:')
for mn, i in zip(MODEL_NAMES, range(len(MODEL_NAMES))):
    print(str(i+1)+'.', mn)

def print_opt():
    print('1. Summary', '2. Predict', sep='\n')

# def plot(model):
#     hs = model.hi
#     # summarize history for categorical accuracy
#     plotter.plot(hs.history['categorical_accuracy'])
#     plotter.plot(hs.history['val_categorical_accuracy'])
#     plotter.title('model accuracy')
#     plotter.ylabel('categorical accuracy')
#     plotter.xlabel('epoch')
#     plotter.legend(['train', 'validation'], loc='upper left')
#     plotter.show()
#     # summarize history for loss
#     plotter.plot(hs.history['loss'])
#     plotter.plot(hs.history['val_loss'])
#     plotter.title('model loss')
#     plotter.ylabel('loss')
#     plotter.xlabel('epoch')
#     plotter.legend(['train', 'validation'], loc='upper left')
#     plotter.show()

def predict(model, crp: list, expected=''):
    in_x = pd.DataFrame([crp])
    prediction = model.predict(in_x)[0]
    print("\nPrediction result: \n")
    df_p = pd.DataFrame([prediction], columns=PRED_COLS)
    print("chord:\n", df_p.idxmax(axis=1))
    print("max:\n", df_p.max(axis=1), end='\n\n')

def to_do_models(opt, model: Model, name: str):
    if opt == 1 or opt == '1':
        print('Name: \"%s\"' % name)
        model.summary()
    elif opt == 2 or opt == '2':
        crp = eval(input('Input a list/vector of CRP chroma: '))
        ch = input('Expect chord?: ')
        if type(crp) is not list or len(crp) != 12:
            print('Error CRP input')
            return 
        predict(model, crp, ch)
    # elif opt == 3 or opt == '3':


model_opt = list(map(str, range(1, len(MODEL_NAMES)+1)))
while True:
    ans = input('Select model (q for quit): ')
    if ans.capitalize() == 'Q':
        break
    if ans in model_opt:
        model = loaded[i-1]
        print_opt()
        to = input('> ')
        to_do_models(to, model, MODEL_NAMES[int(ans)-1])

