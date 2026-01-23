#questo per responde time scaling con rette analitico
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# ---------------------------------------------------------
# CONFIGURAZIONE SOGLIE (I valori K per le rette tratteggiate)
# ---------------------------------------------------------
TARGET_RESPONSE_TIMES = {
    'System': 12.0303,
    'A': 2.228477,
    'B': 1.50661,
    'P': 3.992471
}

# Nome esatto della metrica nel tuo CSV per il Response Time
RT_METRIC_NAME = 'ResponseTime'

# ---------------------------------------------------------
# 1. Caricamento Dati
# ---------------------------------------------------------
try:
    df = pd.read_csv('sample_data/transitorio.csv')
except FileNotFoundError:
    print("Errore: File non trovato. Controlla il percorso.")
    df = pd.DataFrame(columns=['Time', 'Metric', 'Value', 'Seed', 'Component'])

# ---------------------------------------------------------
# Conversione e Pre-processing
# ---------------------------------------------------------
if not df.empty:
    df['Time'] = df['Time'] / 3600
    df['Seed'] = df['Seed'].astype(str)
    unique_seeds = sorted(df['Seed'].unique())
else:
    unique_seeds = []
    available_metrics = []

# 2. Setup dello Stile
sns.set_theme(style="whitegrid")
plt.rcParams.update({'font.size': 10})

# ---------------------------------------------------------
# 3. FILTRO METRICHE
# ---------------------------------------------------------
available_metrics = []
if not df.empty:
    all_metrics_in_file = df['Metric'].unique()

    if RT_METRIC_NAME in all_metrics_in_file:
        available_metrics = [RT_METRIC_NAME]
    else:
        print(f"ATTENZIONE: La metrica '{RT_METRIC_NAME}' non è stata trovata nel file CSV.")
        print(f"Metriche trovate nel file: {all_metrics_in_file}")

components = ['System', 'A', 'B', 'P']

# 4. Ciclo Principale
for metric in available_metrics:

    fig, axes = plt.subplots(2, 2, figsize=(14, 8), sharex=True)

    plot_mapping = {
        'System': axes[0, 0],
        'A':      axes[0, 1],
        'B':      axes[1, 0],
        'P':      axes[1, 1]
    }

    plt.suptitle(f"Metric Analysis: {metric}", y=1.02, fontsize=14, fontweight='bold')

    for comp in components:

        if metric == 'WaitTime' and comp == 'System':
            plot_mapping['System'].set_visible(False)
            continue

        if comp in plot_mapping:
            ax = plot_mapping[comp]
            data_subset = df[(df['Component'] == comp) & (df['Metric'] == metric)]

            if not data_subset.empty:
                sns.lineplot(
                    data=data_subset,
                    x='Time',
                    y='Value',
                    hue='Seed',
                    hue_order=unique_seeds,
                    dashes=False,
                    ax=ax,
                    linewidth=1.0,
                    palette="tab10",
                    alpha=0.8
                )

                # Disegno retta target SOLO se siamo nel grafico del Response Time
                if metric == RT_METRIC_NAME and comp in TARGET_RESPONSE_TIMES:
                    k_val = TARGET_RESPONSE_TIMES[comp]

                    # Senza label, matplotlib non aggiunge questo elemento alla legenda.
                    ax.axhline(y=k_val, color='black', linestyle='--', linewidth=1.5, alpha=0.9)

                ax.set_title(f"Component: {comp}", loc='left', fontweight='bold')
                ax.set_ylabel(f"Mean {metric}")
                ax.set_xlabel("Time (h)")
                ax.set_xlim(left=0)

                # Legenda
                if comp == 'A':
                    # Qui verrà generata la legenda solo per gli elementi che hanno una label (i Seed)
                    ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0., title='Seed')
                else:
                    if ax.get_legend() is not None:
                        ax.get_legend().remove()
            else:
                pass

    plt.tight_layout()

# 5. Mostra tutto
if available_metrics:
    plt.show()
