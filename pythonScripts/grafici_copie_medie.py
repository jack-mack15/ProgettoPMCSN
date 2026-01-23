# codice per grafico copie medie

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import io
from google.colab import files
import numpy as np

# ---------------------------------------------------------
# 1. Caricamento del File
# ---------------------------------------------------------
print("CSV:")
uploaded = files.upload()

if uploaded:
    filename = next(iter(uploaded))
    df = pd.read_csv(io.BytesIO(uploaded[filename]))
else:
    print("Nessun file caricato.")
    df = pd.DataFrame()

# ---------------------------------------------------------
# 2. Generazione del Grafico
# ---------------------------------------------------------
if not df.empty:
    sns.set_theme(style="whitegrid")

    plt.figure(figsize=(10, 6))

    df = df.sort_values(by='MaxNumCopy')

    plt.errorbar(
        x=df['MaxNumCopy'],
        y=df['CopyMean'],
        yerr=df['CI'],  
        fmt='-o',       
        linewidth=2,
        markersize=6,
        capsize=4,      
        color='tab:blue',
        ecolor='tab:red' 
    )

    
    plt.title("Numero di Copie medie con C variabile", fontsize=14, fontweight='bold')
    plt.xlabel("Parametro C", fontsize=12)
    plt.ylabel("Numero medio Copie", fontsize=12)

    # questo per mostrare asse x da 1 a 15
    plt.xticks(np.arange(min(df['MaxNumCopy']), max(df['MaxNumCopy'])+1, 1.0))

    plt.grid(True, linestyle='--', alpha=0.7)
    plt.legend()

    plt.tight_layout()
    plt.show()

else:
    print("Error: con i file")