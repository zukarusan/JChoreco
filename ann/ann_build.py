print('Setting up.', end='\r')
import os
import pandas as pd
from tensorflow import config, distribute
import matplotlib.pyplot as plotter 

print('Setting up..', end='\r')
from tensorflow.keras import Input, Sequential, layers, regularizers
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder, StandardScaler, MinMaxScaler
from pandas import DataFrame
DATASET_DIR = os.path.join('.', 'dataset')
print('Setting up...', end='\r')
X_train = pd.read_csv(os.path.join(DATASET_DIR, '_IV-raw-training_.csv'))
X_test = pd.read_csv(os.path.join(DATASET_DIR, '_IV-raw-testing_.csv'))
y_train = pd.read_csv(os.path.join(DATASET_DIR, '_DV-training_.csv'))
y_test = pd.read_csv(os.path.join(DATASET_DIR, '_DV-testing_.csv'))
print('Set up complete.')

# X_train = X_train.drop('time', axis=1)
# X_test = X_test.drop('time', axis=1)

print("Num GPUs Available: ", len(config.list_physical_devices('GPU')))
mirrored_strategy = distribute.MirroredStrategy()

with mirrored_strategy.scope():
    print('\nBuilding model.', end='\r')
    model = Sequential()
    model.add(Input(shape=(X_train.shape[1],)))
    print('Building model..', end='\r')
    model.add(layers.Dense(units=12, activation='relu', kernel_regularizer=regularizers.l2(0.0001)))
    model.add(layers.Dense(units=84, activation='relu', kernel_regularizer=regularizers.l2(0.01)))
    model.add(layers.Dense(units=48, activation='tanh', kernel_regularizer=regularizers.l2(0.001)))
    model.add(layers.Dense(units=24, activation='relu', kernel_regularizer=regularizers.l2(0.001)))
    print('Building model...', end='\r')
    model.add(layers.Dense(units=y_train.shape[1], activation='softmax'))
    print('Built model. Model summary:')
model.summary()

input('\nPress enter to compile model...')

print('Compiling model...', end='\r')
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['categorical_accuracy', 'categorical_crossentropy'])
print('Model compiled. Optimizer: adam, Loss Function: multi-class cross-entropy, Metrics: categorical accuracy')

input('\nPress enter to train model...')

X_test, X_val, y_test, y_val = train_test_split(
        X_test, y_test, stratify=y_test, test_size=0.5)

hs = model.fit(
    X_train,
    y_train,
    batch_size=300,
    epochs=450,
    validation_data=(X_val, y_val),
    shuffle=True
)

print("Summary history training (plot):")
# summarize history for categorical accuracy
plotter.plot(hs.history['categorical_accuracy'])
plotter.plot(hs.history['val_categorical_accuracy'])
plotter.title('model accuracy')
plotter.ylabel('categorical accuracy')
plotter.xlabel('epoch')
plotter.legend(['train', 'validation'], loc='upper left')
plotter.show()
# summarize history for loss
plotter.plot(hs.history['loss'])
plotter.plot(hs.history['val_loss'])
plotter.title('model loss')
plotter.ylabel('loss')
plotter.xlabel('epoch')
plotter.legend(['train', 'validation'], loc='upper left')
plotter.show()
# summarize history for categorical cross-entropy
plotter.plot(hs.history['categorical_crossentropy'])
plotter.title('categorical cross-entropy')
plotter.ylabel('cat cross-entropy')
plotter.xlabel('epoch')
plotter.legend(['train', 'validation'], loc='upper left')
plotter.show()

input("\nPress enter to eveluate..")

print("Evaluating on test data...")
results = model.evaluate(X_test, y_test, batch_size=12)
print("test loss, test acc:", results)

input("\nPress enter to predict..")

print("Generating predictions for 3 samples")
in_x = DataFrame(X_test[:3])
in_y = DataFrame(y_test[:3])
predictions = model.predict(X_test[:3])
print("predictions shape:", predictions.shape)
print("Predicted input:\n", in_x, in_y.idxmax(axis=1), sep='\n')
print("\nPrediction result: \n")
cols = list(in_y.columns.values)

for p in predictions:
    df_p = DataFrame([p], columns=cols)
    print("chord:\n", df_p.idxmax(axis=1))
    print("max:\n", df_p.max(axis=1), end='\n\n')

ans = input("\n\nSave model (y/n)? : ")
if ans.capitalize() == 'Y':
    if not os.path.exists('saved_model'):
        os.mkdir('saved_model')
    name = input('Name model: ')
    model.save('saved_model/'+name)