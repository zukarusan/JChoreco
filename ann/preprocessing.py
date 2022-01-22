print('Setting up.', end='\r')
import os
import pandas as pd
import time
import numpy
from scipy.sparse.csr import csr_matrix
print('Setting up..', end='\r')
from pandas.core.frame import DataFrame, Series
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler, MinMaxScaler
from sklearn.model_selection import train_test_split
print('Setting up...', end='\r')
DATASET_DIR = os.path.join('.', 'dataset')
DATASET_PATH = os.path.join(DATASET_DIR, '_SHUFFLED-HEADER_.csv')
print('Setup complete.')

def encode_dependent(dv_set: Series) -> None:
    ct = ColumnTransformer(transformers=[('encoder', OneHotEncoder(), [0])], remainder='passthrough')
    out = DataFrame(dv_set)
    return (ct.fit_transform(out), ct.get_feature_names())

def split(dataset_path: str):
    dataset = pd.read_csv(dataset_path)
    columns = dataset.columns.values
    iv = dataset.iloc[:, :-1]
    iv = iv.drop('time', axis=1)                                    # TIME EXCLUDE
    columns = numpy.delete(columns, numpy.where(columns=='time'))   # TIME EXCLUDE COLUMN
    dv = dataset.iloc[:, -1]
    dv, en_names = encode_dependent(dv)
    dv = csr_matrix(dv)
    return (columns, en_names, train_test_split(iv, dv.toarray(), random_state=int(time.time()), stratify=dv.toarray())) # check if stratified correctly after onehot encoded

def DV_to_csv(dv_train, dv_test, columns, dest_dir: str) -> tuple[DataFrame, DataFrame]:
    _dv_train = pd.DataFrame(dv_train, columns=columns)
    _dv_test = pd.DataFrame(dv_test, columns=columns)
    dest_dv_train = os.path.join(dest_dir, '_DV-training_.csv')
    dest_dv_test = os.path.join(dest_dir, '_DV-testing_.csv')
    _dv_train.to_csv(dest_dv_train, index=False)
    _dv_test.to_csv(dest_dv_test, index=False)
    return (_dv_train, _dv_test)

def IV_raw_to_csv(iv_train, iv_test, columns, dest_dir: str) -> tuple[DataFrame, DataFrame]:
    _iv_train = pd.DataFrame(iv_train, columns=columns)
    _iv_test = pd.DataFrame(iv_test, columns=columns)
    dest_iv_train = os.path.join(dest_dir, '_IV-raw-training_.csv')
    dest_iv_test = os.path.join(dest_dir, '_IV-raw-testing_.csv')
    _iv_train.to_csv(dest_iv_train, index=False)
    _iv_test.to_csv(dest_iv_test, index=False)
    return (_iv_train, _iv_test)
    
def IV_standardized_to_csv(iv_train, iv_test, columns, dest_dir: str) -> tuple[DataFrame, DataFrame]:
    sc = StandardScaler()
    _iv_train = pd.DataFrame(sc.fit_transform(iv_train), columns=columns)
    _iv_test = pd.DataFrame(sc.transform(iv_test), columns=columns)
    dest_iv_train = os.path.join(dest_dir, '_IV-scaled-training_.csv')
    dest_iv_test = os.path.join(dest_dir, '_IV-scaled-testing_.csv')
    _iv_train.to_csv(dest_iv_train, index=False)
    _iv_test.to_csv(dest_iv_test, index=False)
    return (_iv_train, _iv_test)

def IV_normalized_to_csv(iv_train, iv_test, columns, dest_dir: str) -> tuple[DataFrame, DataFrame]:
    nm = MinMaxScaler()
    _iv_train = pd.DataFrame(nm.fit_transform(iv_train), columns=columns)
    _iv_test = pd.DataFrame(nm.transform(iv_test), columns=columns)
    dest_iv_train = os.path.join(dest_dir, '_IV-norm-training_.csv')
    dest_iv_test = os.path.join(dest_dir, '_IV-norm-testing_.csv')
    _iv_train.to_csv(dest_iv_train, index=False)
    _iv_test.to_csv(dest_iv_test, index=False)
    return [_iv_train, _iv_test]


def main():
    columns, en_columns, (iv_train, iv_test, dv_train, dv_test) = split(DATASET_PATH)
    preH = "encoder__x0_"
    en_columns = [c[len(preH):] for c in en_columns]
    DV_to_csv(dv_train, dv_test, en_columns, DATASET_DIR)
    IV_raw_to_csv(iv_train, iv_test, columns[:-1], DATASET_DIR)
    IV_standardized_to_csv(iv_train, iv_test, columns[:-1], DATASET_DIR)
    IV_normalized_to_csv(iv_train, iv_test, columns[:-1], DATASET_DIR)

if __name__ == '__main__':
    main()