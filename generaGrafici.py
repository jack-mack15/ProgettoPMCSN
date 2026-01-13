import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# 1. Caricamento Dati
try:
    # Assicurati che il percorso sia corretto
    df = pd.read_csv('sample_data/Transitorio.csv')
except FileNotFoundError:
    print("Errore: File non trovato. Controlla il percorso.")
    df = pd.DataFrame(columns=['Time', 'ResponseTime', 'Seed', 'Component'])

# ---------------------------------------------------------
# NUOVA MODIFICA: Conversione da Secondi a Ore
# ---------------------------------------------------------
if not df.empty:
    # Divide tutti i valori della colonna 'Time' per 3600
    df['Time'] = df['Time'] / 3600 

    # Conversione del Seed in stringa per i colori
    df['Seed'] = df['Seed'].astype(str)

# 2. Setup dello Stile
sns.set_theme(style="whitegrid")
plt.rcParams.update({'font.size': 10})

# 3. Creazione della figura
fig, axes = plt.subplots(2, 2, figsize=(14, 8), sharex=True)

plot_mapping = {
    'System': axes[0, 0],
    'A':      axes[0, 1],
    'B':      axes[1, 0],
    'P':      axes[1, 1]
}

components = ['System', 'A', 'B', 'P']

# 4. Ciclo di generazione grafici
for comp in components:
    if comp in plot_mapping and not df.empty:
        ax = plot_mapping[comp]
        
        data_subset = df[df['Component'] == comp]
        
        # PLOTTING
        sns.lineplot(
            data=data_subset, 
            x='Time', 
            y='ResponseTime', 
            hue='Seed', 
            dashes=False,    
            ax=ax,
            linewidth=1.5,
            palette="tab10"
            # Rimossi marker e markersize (niente puntini)
        )
        
        # Personalizzazione Assi
        ax.set_title(comp, loc='left', fontweight='bold')
        ax.set_ylabel("Mean response time (s)")
        ax.set_xlabel("Time (h)") # L'etichetta è già corretta in ore

        # Impostare l'origine degli assi a 0
        ax.set_xlim(left=0)      
        ax.set_ylim(bottom=0)    
        
        # Gestione Legenda
        if comp == 'A':
            ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0., title='Seed')
        else:
            if ax.get_legend() is not None:
                ax.get_legend().remove()

# 5. Output
plt.tight_layout()
plt.suptitle("Response time of transient state", y=1.02, fontsize=14)
plt.show()