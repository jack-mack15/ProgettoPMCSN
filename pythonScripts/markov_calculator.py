#QUESTO PER VERIFICA SCALING
import numpy as np

# 1. Parametri
lam = 1.2   # lambda
mu = 1.25   # mu

# qui ci stanno i pi greco
# Ordine: p0, p1, p2, p3, p01, p02, p03, p11, p12, p13, p21, p22, p23, p31, p32, p33
vars_map = {
    'p0': 0, 'p1': 1, 'p2': 2, 'p3': 3,
    'p01': 4, 'p02': 5, 'p03': 6,
    'p11': 7, 'p12': 8, 'p13': 9,
    'p21': 10, 'p22': 11, 'p23': 12,
    'p31': 13, 'p32': 14, 'p33': 15
}
n_vars = len(vars_map)

# coefficienti equazioni
c1 = mu / lam
c2_lam = lam / (lam + mu)
c2_mu  = mu  / (lam + mu)
c3_lam = lam / (lam + 2*mu)
c3_mu  = mu  / (lam + 2*mu)
c4_spec = lam / (2*mu)

#Matrice A (16x16) e del vettore b (16)
A = np.zeros((n_vars, n_vars))
b = np.zeros(n_vars)

# per aggiungere equazioni
def add_eq(row_idx, positive_var, negative_terms):
    A[row_idx, vars_map[positive_var]] = 1.0
    for coeff, var_name in negative_terms:
        A[row_idx, vars_map[var_name]] = -coeff

add_eq(0, 'p0', [(c1, 'p1'), (c1, 'p01')])
add_eq(1, 'p1', [(c2_lam, 'p0'), (c2_mu, 'p11'), (c2_mu, 'p2')])
add_eq(2, 'p2', [(c2_lam, 'p1'), (c2_mu, 'p3'), (c2_mu, 'p21')])
add_eq(3, 'p3', [(c2_lam, 'p2'), (c2_mu, 'p31')])
add_eq(4, 'p31', [(c3_lam, 'p21'), (c3_lam, 'p3'), (c3_mu, 'p32')])
add_eq(5, 'p32', [(c3_lam, 'p22'), (c3_lam, 'p31'), (c3_mu, 'p33')])
add_eq(6, 'p33', [(c4_spec, 'p23'), (c4_spec, 'p32')])
add_eq(7, 'p23', [(c3_lam, 'p13'), (c3_mu, 'p33')])
add_eq(8, 'p13', [(c3_lam, 'p03'), (c3_mu, 'p23')])
add_eq(9, 'p03', [(c2_mu, 'p13')])
add_eq(10, 'p02', [(c2_mu, 'p03'), (c2_mu, 'p12')])
add_eq(11, 'p01', [(c2_mu, 'p02'), (c2_mu, 'p11')])
add_eq(12, 'p11', [(c3_lam, 'p01'), (c3_mu, 'p21'), (c3_mu, 'p12')])
add_eq(13, 'p21', [(c3_lam, 'p11'), (c3_mu, 'p31'), (c3_mu, 'p22')])
add_eq(14, 'p22', [(c3_lam, 'p12'), (c3_mu, 'p23'), (c3_mu, 'p32')])
add_eq(15, 'p12', [(c3_lam, 'p02'), (c3_mu, 'p13'), (c3_mu, 'p22')])

# qui fa la normalizzazion
A[15, :] = 1.0
b[15] = 1.0

try:
    probabilities = np.linalg.solve(A, b)

    print(f"Risultati con Lambda={lam}, Mu={mu}:\n")

    idx_to_name = {v: k for k, v in vars_map.items()}

    sorted_keys = sorted(vars_map.keys())

    prob_sum = 0
    for name in sorted_keys:
        val = probabilities[vars_map[name]]
        prob_sum += val
        print(f"{name}: {val:.5f}")

    print(f"\nVerifica somma totale: {prob_sum:.5f}")

except np.linalg.LinAlgError:
    print("errore")



# 1. calcolo E[N]
E_N = 0
for name, idx in vars_map.items():
    if name == 'p0':
        i, j = 0, 0
    elif len(name) == 2:
        i = int(name[1])
        j = 0
    elif len(name) == 3: # p12 -> i=1, j=2
        i = int(name[1])
        j = int(name[2])

    prob = probabilities[idx]
    num_users = i + j
    E_N += num_users * prob

# calcolo thr
p_loss = probabilities[vars_map['p33']]
lambda_eff = lam * (1 - p_loss)

# tempo risp
E_T = E_N / lambda_eff

print("-" * 30)
print(f"Probabilità di Perdita (P_loss): {p_loss:.5f}")
print(f"Throughput Effettivo (Lambda_eff): {lambda_eff:.5f} job/s")
print(f"Numero Medio Utenti (E[N]): {E_N:.5f}")
print(f"TEMPO DI RISPOSTA MEDIO (E[T]): {E_T:.5f} s")
print("-" * 30)