#grafici per lambda crescente

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import io
from google.colab import files

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
# 2. Configurazione Grafica e Nomi
# ---------------------------------------------------------
if not df.empty:
    sns.set_theme(style="whitegrid")

    # Mappa: "Nome nel CSV" : "Titolo nel Grafico"
    metric_labels = {
        'rt': 'Response Time',
        'thr': 'Throughput',
        'util': 'Utilization',
        'pop': 'Population',
        'wait': 'Waiting Time'
    }

    component_order = ['A', 'B', 'P', 'System']
    unique_metrics = df['Metric'].unique()

    # ---------------------------------------------------------
    # 3. Ciclo per generare i grafici
    # ---------------------------------------------------------
    for metric in unique_metrics:

        display_name = metric_labels.get(metric, metric)

        fig, axes = plt.subplots(2, 2, figsize=(14, 10), sharex=False)

        # Usa il display_name per il titolo generale
        fig.suptitle(f"Results for Metric: {display_name}", fontsize=16, fontweight='bold', y=1.02)

        axes_flat = axes.flatten()

        for i, comp in enumerate(component_order):
            ax = axes_flat[i]
            subset = df[(df['Metric'] == metric) & (df['Component'] == comp)]
            subset = subset.sort_values(by='Lambda')

            if not subset.empty:
                ax.errorbar(
                    x=subset['Lambda'],
                    y=subset['Value'],
                    yerr=subset['CI'],
                    fmt='-o',
                    linewidth=2,
                    markersize=6,
                    capsize=5,
                    capthick=1.5,
                    elinewidth=1.5,
                    color='tab:blue',
                    ecolor='tab:red',
                    label=f'{comp} Trend'
                )

                ax.set_title(f"Component: {comp}", fontweight='bold')

                # Usa il display_name anche per l'asse Y
                ax.set_ylabel(display_name)
                ax.set_xlabel("Lambda")
                ax.grid(True, linestyle='--', alpha=0.7)

            else:
                # Se non ci sono dati, nascondi completamente il grafico
                ax.set_visible(False)

        plt.tight_layout()
        plt.show()
        print("\n" + "="*80 + "\n")

else:
    print("DataFrame vuoto.")