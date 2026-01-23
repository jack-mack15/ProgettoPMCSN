# SOLO PER SCALING CON C variabile
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

        # sharex=False per evitare conflitti con grafici nascosti
        fig, axes = plt.subplots(2, 2, figsize=(14, 10), sharex=False)

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
                    linewidth=1, #2,
                    markersize=3, #6,
                    capsize=2, #5,
                    capthick= 0.5,#1.5,
                    elinewidth= 0.5, #1.5,
                    color='tab:blue',
                    ecolor='tab:red',
                    label=f'{comp} Trend'
                )

                ax.set_title(f"Component: {comp}", fontweight='bold')
                ax.set_ylabel(display_name)
                ax.set_xlabel("C")
                ax.grid(True, linestyle='--', alpha=0.7)

                # --- MODIFICA RICHIESTA ---
                # componente è A o P, forziamo l'asse Y a partire da 0.
                if comp in ['A', 'P']:
                    # media + intervallo confidenza
                    max_val = (subset['Value'] + subset['CI']).max()

                    ax.set_ylim(bottom=0, top=max_val * 1.1)

            else:
                # nasconde il grafico se vuoto
                ax.set_visible(False)

        plt.tight_layout()
        plt.show()
        print("\n" + "="*80 + "\n")

else:
    print("DataFrame vuoto.")