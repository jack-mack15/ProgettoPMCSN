# codice per unire due grafici, quello blu il precedente e il rosso quello corrente
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import io
from google.colab import files

# ---------------------------------------------------------
# 1. Caricamento dei Due File
# ---------------------------------------------------------
print("OCCHIO: CARICAMENTO MODELLO 1 (Curva BLU)")
uploaded_1 = files.upload()

if uploaded_1:
    filename_1 = next(iter(uploaded_1))
    df1 = pd.read_csv(io.BytesIO(uploaded_1[filename_1]))
    print(f"Modello 1 caricato: {filename_1}")
else:
    print("Nessun file caricato per Modello 1.")
    df1 = pd.DataFrame()

print("\nOCCHIO CARICAMENTO MODELLO 2 (Curva ROSSA)")
uploaded_2 = files.upload()

if uploaded_2:
    filename_2 = next(iter(uploaded_2))
    df2 = pd.read_csv(io.BytesIO(uploaded_2[filename_2]))
    print(f"Modello 2 caricato: {filename_2}")
else:
    print("Nessun file caricato per Modello 2.")
    df2 = pd.DataFrame()

# ---------------------------------------------------------
# 2. Configurazione Grafica e Nomi
# ---------------------------------------------------------
if not df1.empty or not df2.empty:
    sns.set_theme(style="whitegrid")

    metric_labels = {
        'rt': 'Response Time',
        'thr': 'Throughput',
        'util': 'Utilization',
        'pop': 'Population',
        'wait': 'Waiting Time'
    }

    component_order = ['A', 'B', 'P', 'System']

    metrics_1 = set(df1['Metric'].unique()) if not df1.empty else set()
    metrics_2 = set(df2['Metric'].unique()) if not df2.empty else set()
    unique_metrics = list(metrics_1.union(metrics_2))

    # ---------------------------------------------------------
    # 3. Ciclo per generare i grafici
    # ---------------------------------------------------------
    for metric in unique_metrics:

        display_name = metric_labels.get(metric, metric)


        fig, axes = plt.subplots(2, 2, figsize=(14, 10), sharex=False)
        fig.suptitle(f"Comparison Results for Metric: {display_name}", fontsize=16, fontweight='bold', y=1.02)

        axes_flat = axes.flatten()

        for i, comp in enumerate(component_order):
            ax = axes_flat[i]

            plot_drawn = False

            # PLOT MODELLO 1 (BLU)
            if not df1.empty:
                subset1 = df1[(df1['Metric'] == metric) & (df1['Component'] == comp)]
                subset1 = subset1.sort_values(by='Lambda')

                if not subset1.empty:
                    plot_drawn = True
                    ax.errorbar(
                        x=subset1['Lambda'],
                        y=subset1['Value'],
                        yerr=subset1['CI'],
                        fmt='-o',
                        linewidth=2,
                        markersize=6,
                        capsize=5,
                        capthick=1.5,
                        elinewidth=1.5,
                        color='tab:red',
                        ecolor='tab:red',
                        label='Modello F2A'
                    )

            # PLOT MODELLO 2 (ROSSO)
            if not df2.empty:
                subset2 = df2[(df2['Metric'] == metric) & (df2['Component'] == comp)]
                subset2 = subset2.sort_values(by='Lambda')

                if not subset2.empty:
                    plot_drawn = True
                    ax.errorbar(
                        x=subset2['Lambda'],
                        y=subset2['Value'],
                        yerr=subset2['CI'],
                        fmt='-o',
                        linewidth=2,
                        markersize=6,
                        capsize=5,
                        capthick=1.5,
                        elinewidth=1.5,
                        color='tab:green',
                        ecolor='tab:green',
                        label='Modello SCALING'
                    )

            # --- GESTIONE VISIBILITÀ ---
            if plot_drawn:
                ax.set_title(f"Component: {comp}", fontweight='bold')
                ax.set_ylabel(display_name)
                ax.set_xlabel("Lambda")
                ax.grid(True, linestyle='--', alpha=0.7)
                ax.legend()
            else:
                #questo sempre per system waiting time
                ax.set_visible(False)

        plt.tight_layout()
        plt.show()
        print("\n" + "="*80 + "\n")

else:
    print("Errorecon i fle")