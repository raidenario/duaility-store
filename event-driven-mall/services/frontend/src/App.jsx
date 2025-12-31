import { Fragment, useEffect, useMemo, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Dialog, Transition } from '@headlessui/react'
import { AnimatePresence, motion } from 'framer-motion'
import clsx from 'classnames'
import './App.css'

const COMMAND_API = 'http://localhost:8080'
const QUERY_API = 'http://localhost:8081'

const productTypes = ['Periférico', 'Eletrônico', 'Acessório']
const buyerUsers = ['comprador-1', 'comprador-2']
const adminUser = 'admin-1'

const flowNodes = {
  frontend: { x: 14, y: 55, label: 'Frontend', tag: 'React' },
  command: { x: 30, y: 40, label: 'Command API', tag: 'Java' },
  kafka: { x: 48, y: 30, label: 'Kafka', tag: 'Event Bus' },
  inventory: { x: 78, y: 26, label: 'Inventory Worker', tag: 'Java' },
  payment: { x: 78, y: 48, label: 'Payment Worker', tag: 'Clojure' },
  projector: { x: 58, y: 60, label: 'Projector Worker', tag: 'Clojure' },
  mongo: { x: 78, y: 70, label: 'MongoDB', tag: 'Read Model' },
  query: { x: 36, y: 70, label: 'Query API', tag: 'Java' },
  client: { x: 52, y: 82, label: 'Storefront', tag: 'React Query' },
}

// Função para criar path curvo entre dois pontos
const createCurvedPath = (x1, y1, x2, y2) => {
  const dx = x2 - x1
  const dy = y2 - y1
  const curvature = 0.3
  
  // Control points para criar uma curva suave
  const cp1x = x1 + dx * 0.5 - dy * curvature
  const cp1y = y1 + dy * 0.5 + dx * curvature
  const cp2x = x1 + dx * 0.5 + dy * curvature
  const cp2y = y1 + dy * 0.5 - dx * curvature
  
  return `M ${x1} ${y1} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${x2} ${y2}`
}

const createEdges = [
  { from: 'frontend', to: 'command', label: 'POST /products', eventName: 'ProductCreatedEvent' },
  { from: 'command', to: 'kafka', label: 'ProductCreatedEvent', eventName: 'ProductCreatedEvent' },
  { from: 'kafka', to: 'projector', label: 'Consume + Project', eventName: 'ProductCreatedEvent' },
  { from: 'projector', to: 'mongo', label: 'Insert Read Model', eventName: 'ProductCreatedEvent' },
  { from: 'mongo', to: 'query', label: 'Expose GET /products', eventName: 'ProductCreatedEvent' },
  { from: 'query', to: 'client', label: 'React Query fetch', eventName: 'ProductCreatedEvent' },
]

const purchaseEdges = [
  { from: 'frontend', to: 'command', label: 'POST /orders', eventName: 'OrderCreatedEvent' },
  { from: 'command', to: 'kafka', label: 'OrderCreatedEvent', eventName: 'OrderCreatedEvent' },
  { from: 'kafka', to: 'inventory', label: 'StockReserved', eventName: 'StockReservedEvent' },
  { from: 'inventory', to: 'payment', label: 'Debit wallet', eventName: 'PaymentProcessedEvent' },
  { from: 'payment', to: 'projector', label: 'PaymentSuccess/Failed', eventName: 'PaymentSuccessEvent' },
  { from: 'projector', to: 'mongo', label: 'Update Read Model', eventName: 'OrderUpdatedEvent' },
  { from: 'mongo', to: 'query', label: 'Expose GET /orders', eventName: 'OrderUpdatedEvent' },
  { from: 'query', to: 'client', label: 'React Query fetch', eventName: 'OrderUpdatedEvent' },
]

const fetchProducts = async () => {
  const res = await fetch(`${QUERY_API}/products`)
  if (!res.ok) throw new Error('Falha ao carregar produtos')
  return res.json()
}

const fetchWallet = async (userId) => {
  const res = await fetch(`${COMMAND_API}/wallets/${userId}`)
  if (!res.ok) throw new Error('Falha ao carregar carteira')
  return res.json()
}

function App() {
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [toast, setToast] = useState('')
  const [role, setRole] = useState('admin')
  const [buyer, setBuyer] = useState(buyerUsers[0])
  const currentUser = role === 'admin' ? adminUser : buyer
  const [form, setForm] = useState({ name: '', price: '', type: productTypes[0] })
  const [trace, setTrace] = useState(null)
  const [traceIndex, setTraceIndex] = useState(0)
  const [activeAlerts, setActiveAlerts] = useState({})
  const traceTimer = useRef(null)
  const activeEdges = role === 'admin' ? createEdges : purchaseEdges

  const { data: products = [], isLoading: loadingProducts } = useQuery({
    queryKey: ['products'],
    queryFn: fetchProducts,
    refetchInterval: 5000,
  })

  const { data: wallet, refetch: refetchWallet } = useQuery({
    queryKey: ['wallet', currentUser],
    queryFn: () => fetchWallet(currentUser),
    enabled: role === 'buyer',
    refetchInterval: 7000,
  })

  useEffect(() => {
    if (!trace) return
    clearTimeout(traceTimer.current)
    if (traceIndex >= trace.length) {
      setTrace(null)
      setTraceIndex(0)
      setActiveAlerts({})
      return
    }
    
    const currentEdge = trace[traceIndex]
    if (currentEdge) {
      // Mostrar alerta no nó de destino
      setActiveAlerts((prev) => ({
        ...prev,
        [currentEdge.to]: {
          message: `${currentEdge.eventName || currentEdge.label} consumido`,
          timestamp: Date.now(),
        },
      }))
      
      // Remover alerta após 2 segundos
      setTimeout(() => {
        setActiveAlerts((prev) => {
          const newAlerts = { ...prev }
          delete newAlerts[currentEdge.to]
          return newAlerts
        })
      }, 2000)
    }
    
    traceTimer.current = setTimeout(() => setTraceIndex((prev) => prev + 1), 800)
    return () => clearTimeout(traceTimer.current)
  }, [trace, traceIndex])

  const startTrace = (edges) => {
    setTrace(edges)
    setTraceIndex(0)
    setActiveAlerts({})
  }

  const createProduct = useMutation({
    mutationFn: async (payload) => {
      const res = await fetch(`${COMMAND_API}/products`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!res.ok) throw new Error(await res.text())
      return res.json()
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      setForm({ name: '', price: '', type: productTypes[0] })
      setIsModalOpen(false)
      setToast('Produto enviado para Command API. Aguarde projeção no Mongo.')
      startTrace(createEdges)
    },
    onError: (err) => setToast(err.message || 'Erro ao criar produto'),
  })

  const buyProduct = useMutation({
    mutationFn: async ({ name, price }) => {
      const res = await fetch(`${COMMAND_API}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: currentUser,
          totalAmount: Number(price),
          items: [name],
        }),
      })
      if (!res.ok) throw new Error(await res.text())
      return res.json()
    },
    onSuccess: () => {
      setToast('Compra enviada. Pagamento será debitado do saldo.')
      refetchWallet()
      startTrace(purchaseEdges)
    },
    onError: (err) => setToast(err.message || 'Erro ao comprar produto'),
  })

  const liveLogs = useMemo(() => {
    return products
      .flatMap((p) =>
        (p.events || []).map((ev) => ({
          productId: p.productId || p.id,
          name: p.name,
          ...ev,
        })),
      )
      .sort((a, b) => new Date(b.at || 0) - new Date(a.at || 0))
      .slice(0, 8)
  }, [products])

  const animatePacket = trace && trace[traceIndex]
  
  // Calcular path para animação da bolinha
  const getPathLength = (edge) => {
    const from = flowNodes[edge.from]
    const to = flowNodes[edge.to]
    const path = createCurvedPath(from.x, from.y, to.x, to.y)
    const pathElement = document.createElementNS('http://www.w3.org/2000/svg', 'path')
    pathElement.setAttribute('d', path)
    return pathElement.getTotalLength()
  }

  return (
    <div className="min-h-screen relative overflow-hidden">
      <div className="blob bg-accent-blue/40 -left-10 top-10" />
      <div className="blob bg-accent-purple/40 right-0 -bottom-10" />

      <header className="flex items-center justify-between px-6 lg:px-10 py-5">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gradient-to-r from-accent-blue to-accent-purple flex items-center justify-center font-display text-lg text-bg-dark shadow-neon">
            DS
          </div>
          <div>
            <p className="text-sm text-slate-400">Duality Store</p>
            <h1 className="text-2xl font-display font-bold">Retail + Architecture Debugger</h1>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => {
              setRole(role === 'admin' ? 'buyer' : 'admin')
              setTrace(null)
              setTraceIndex(0)
              setActiveAlerts({})
            }}
            className="px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-sm"
          >
            Modo: {role === 'admin' ? 'Cadastrar produtos' : 'Comprar produtos'}
          </button>
          {role === 'buyer' && (
            <select
              value={buyer}
              onChange={(e) => setBuyer(e.target.value)}
              className="px-3 py-2 rounded-xl bg-white/5 border border-white/10 text-sm"
            >
              {buyerUsers.map((u) => (
                <option key={u}>{u}</option>
              ))}
            </select>
          )}
          {role === 'buyer' && (
            <div className="px-3 py-2 rounded-xl bg-white/5 border border-white/10 text-sm">
              Saldo: R$ {wallet ? Number(wallet.balance).toFixed(2) : '...'}
            </div>
          )}
        </div>
      </header>

      <main className="grid lg:grid-cols-[3fr_2fr] gap-4 lg:gap-6 px-4 lg:px-10 pb-12">
        <section className="space-y-4 lg:space-y-6">
          <div className="glass rounded-2xl p-6 relative overflow-hidden">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <p className="text-sm text-slate-400 uppercase tracking-[0.2em]">Retail Storefront</p>
                <h2 className="text-3xl font-display font-semibold">Catálogo em tempo real</h2>
                <p className="text-slate-400 mt-2 max-w-2xl">
                  Crie produtos no lado de escrita (Postgres/Kafka) e veja a projeção surgir no Mongo via Query API.
                </p>
              </div>
              {role === 'admin' && (
                <button
                  onClick={() => setIsModalOpen(true)}
                  className="px-4 py-2 rounded-xl bg-gradient-to-r from-accent-blue to-accent-purple text-sm font-semibold shadow-neon"
                >
                  Criar Produto
                </button>
              )}
            </div>
          </div>

          <div className="grid md:grid-cols-3 gap-3">
            {['Kafka', 'Postgres (event store)', 'MongoDB (read)'].map((item) => (
              <div key={item} className="glass rounded-xl p-4">
                <p className="text-xs text-slate-400 uppercase tracking-[0.2em]">{item}</p>
                <div className="mt-2 flex items-baseline gap-2">
                  <span className="text-xl font-semibold text-accent-green">OK</span>
                  <span className="text-xs text-slate-400">latência baixa</span>
                </div>
              </div>
            ))}
          </div>

          <div className="glass rounded-2xl p-4 md:p-6">
            <div className="flex items-center justify-between mb-4">
              <div>
                <p className="text-xs text-slate-400 uppercase tracking-[0.2em]">Produtos</p>
                <h3 className="text-xl font-semibold">Lista projetada (Mongo)</h3>
              </div>
              <button
                onClick={() => queryClient.invalidateQueries({ queryKey: ['products'] })}
                className="text-sm text-accent-blue hover:underline"
              >
                Atualizar
              </button>
            </div>

            {loadingProducts ? (
              <p className="text-slate-400">Carregando produtos...</p>
            ) : (
              <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-4">
                {products.map((p) => (
                  <div key={p.productId || p.id} className="rounded-xl bg-bg-card/70 border border-white/10 p-4 space-y-3">
                    <div>
                      <p className="text-xs text-slate-400 uppercase tracking-[0.15em]">{p.type}</p>
                      <h4 className="font-semibold text-lg">{p.name}</h4>
                    </div>
                    <p className="text-sm text-slate-300">R$ {Number(p.price).toFixed(2)}</p>
                    <div className="text-xs text-slate-400">
                      Criado em {p.createdAt ? new Date(p.createdAt).toLocaleString() : '—'}
                    </div>
                    <div className="flex flex-wrap gap-1 text-[11px] text-slate-400">
                      {(p.events || []).map((ev, idx) => (
                        <span key={idx} className="px-2 py-1 rounded-full bg-white/5 border border-white/10">
                          {ev.stage}
                        </span>
                      ))}
                    </div>
                    {role === 'buyer' ? (
                      <button
                        className="w-full rounded-lg bg-gradient-to-r from-accent-blue to-accent-purple py-2 text-sm font-semibold disabled:opacity-50"
                        onClick={() => buyProduct.mutate({ name: p.name, price: p.price })}
                        disabled={buyProduct.isPending}
                      >
                        {buyProduct.isPending ? 'Comprando...' : 'Comprar'}
                      </button>
                    ) : (
                      <div className="text-xs text-slate-500">Modo admin: só listando.</div>
                    )}
                  </div>
                ))}
                {products.length === 0 && <p className="text-slate-400">Nenhum produto projetado ainda.</p>}
              </div>
            )}
          </div>
        </section>

        <section className="space-y-4 lg:space-y-6">
          <div className="glass rounded-2xl p-4 lg:p-6 relative overflow-hidden">
            <div className="flex items-center justify-between mb-4">
              <div>
                <p className="text-xs text-slate-400 uppercase tracking-[0.2em]">Architecture Debugger</p>
                <h3 className="text-xl font-semibold">Fluxo Kafka/CQRS animado</h3>
              </div>
              <div className="flex items-center gap-2 text-xs text-slate-400">
                <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
                baseado em eventos do backend
              </div>
            </div>

            <div className="relative h-[500px] rounded-xl bg-gradient-to-br from-white/5 to-black/20 border border-white/10 overflow-hidden">
              <svg className="absolute inset-0 w-full h-full" viewBox="0 0 100 100" preserveAspectRatio="xMidYMid meet">
                <defs>
                  <linearGradient id="edgeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor="#58a6ff" stopOpacity="0.6" />
                    <stop offset="50%" stopColor="#a371f7" stopOpacity="0.8" />
                    <stop offset="100%" stopColor="#3fb950" stopOpacity="0.6" />
                  </linearGradient>
                  <filter id="glow">
                    <feGaussianBlur stdDeviation="1" result="coloredBlur"/>
                    <feMerge>
                      <feMergeNode in="coloredBlur"/>
                      <feMergeNode in="SourceGraphic"/>
                    </feMerge>
                  </filter>
                </defs>
                
                {/* Renderizar todas as linhas curvas */}
                {activeEdges.map((edge, idx) => {
                  const from = flowNodes[edge.from]
                  const to = flowNodes[edge.to]
                  const path = createCurvedPath(from.x, from.y, to.x, to.y)
                  const isActive = animatePacket?.from === edge.from && animatePacket?.to === edge.to
                  
                  return (
                    <g key={idx}>
                      {/* Linha de fundo (sempre visível, mais opaca) */}
                      <path
                        d={path}
                        fill="none"
                        stroke="url(#edgeGradient)"
                        strokeWidth="0.3"
                        strokeOpacity="0.2"
                        strokeLinecap="round"
                        className="edge-path-base"
                      />
                      {/* Linha ativa (quando animada) */}
                      {isActive && (
                        <motion.path
                          d={path}
                          fill="none"
                          stroke="url(#edgeGradient)"
                          strokeWidth="0.5"
                          strokeOpacity="0.9"
                          strokeLinecap="round"
                          filter="url(#glow)"
                          initial={{ pathLength: 0 }}
                          animate={{ pathLength: 1 }}
                          transition={{ duration: 0.8, ease: 'easeInOut' }}
                        />
                      )}
                      {/* Label da linha */}
                      {isActive && (
                        <motion.text
                          x={(from.x + to.x) / 2}
                          y={(from.y + to.y) / 2 - 1}
                          className="edge-label"
                          textAnchor="middle"
                          initial={{ opacity: 0, scale: 0.8 }}
                          animate={{ opacity: 1, scale: 1 }}
                          exit={{ opacity: 0 }}
                        >
                          {edge.label}
                        </motion.text>
                      )}
                    </g>
                  )
                })}
              </svg>

              {/* Renderizar nós com alertas */}
              {Object.entries(flowNodes).map(([key, node]) => {
                const alert = activeAlerts[key]
                const isActive = animatePacket?.to === key
                
                return (
                  <div key={key} className="absolute -translate-x-1/2 -translate-y-1/2" style={{ left: `${node.x}%`, top: `${node.y}%` }}>
                    <motion.div
                      className="relative"
                      animate={isActive ? { scale: 1.1 } : { scale: 1 }}
                      transition={{ duration: 0.3 }}
                    >
                      <div className="w-32 rounded-xl bg-white/10 border-2 border-white/20 p-2 text-center backdrop-blur-sm shadow-lg">
                        <p className="text-xs text-slate-300 font-medium">{node.tag}</p>
                        <p className="font-semibold text-sm text-white">{node.label}</p>
                      </div>
                      
                      {/* Alerta quando evento é consumido */}
                      <AnimatePresence>
                        {alert && (
                          <motion.div
                            initial={{ opacity: 0, y: -10, scale: 0.8 }}
                            animate={{ opacity: 1, y: -35, scale: 1 }}
                            exit={{ opacity: 0, y: -20, scale: 0.8 }}
                            className="absolute left-1/2 -translate-x-1/2 whitespace-nowrap px-3 py-1.5 rounded-lg bg-gradient-to-r from-accent-blue to-accent-purple text-xs font-semibold shadow-neon z-50"
                          >
                            {alert.message}
                            <div className="absolute bottom-0 left-1/2 -translate-x-1/2 translate-y-full w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-accent-purple"></div>
                          </motion.div>
                        )}
                      </AnimatePresence>
                    </motion.div>
                  </div>
                )
              })}

              {/* Bolinha animada seguindo o path */}
              <AnimatePresence>
                {animatePacket && (() => {
                  const from = flowNodes[animatePacket.from]
                  const to = flowNodes[animatePacket.to]
                  const path = createCurvedPath(from.x, from.y, to.x, to.y)
                  
                  return (
                    <motion.div
                      key={`${traceIndex}-${animatePacket.from}-${animatePacket.to}`}
                      className="absolute w-4 h-4 rounded-full bg-gradient-to-r from-accent-blue to-accent-purple pulse-dot z-40"
                      style={{
                        left: `${from.x}%`,
                        top: `${from.y}%`,
                      }}
                      animate={{
                        left: [`${from.x}%`, `${to.x}%`],
                        top: [`${from.y}%`, `${to.y}%`],
                      }}
                      transition={{
                        duration: 0.8,
                        ease: [0.43, 0.13, 0.23, 0.96], // Curva de easing suave
                      }}
                    />
                  )
                })()}
              </AnimatePresence>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-3">
              {activeEdges.map((edge, idx) => {
                const isActive = animatePacket?.from === edge.from && animatePacket?.to === edge.to
                return (
                  <div
                    key={idx}
                    className={clsx(
                      'rounded-xl border p-3 text-sm transition-all duration-300',
                      isActive
                        ? 'bg-white/15 border-accent-blue/80 shadow-lg shadow-accent-blue/20'
                        : 'bg-white/5 border-white/10',
                    )}
                  >
                    <p className="text-xs text-slate-400">{edge.label}</p>
                    <p className="font-semibold text-xs mt-1">
                      {edge.from} ➜ {edge.to}
                    </p>
                  </div>
                )
              })}
            </div>
          </div>

          <div className="glass rounded-2xl p-4 lg:p-6">
            <div className="flex items-center justify-between mb-3">
              <div>
                <p className="text-xs text-slate-400 uppercase tracking-[0.2em]">Logs do backend</p>
                <h3 className="text-lg font-semibold">Derivados dos eventos Kafka projetados</h3>
              </div>
            </div>
            <div className="space-y-3 max-h-[260px] overflow-auto pr-1">
              {liveLogs.length === 0 && <p className="text-slate-400 text-sm">Sem logs ainda.</p>}
              {liveLogs.map((log) => (
                <div key={`${log.productId}-${log.at}`} className="rounded-xl bg-white/5 border border-white/10 p-3">
                  <div className="flex items-center justify-between text-sm">
                    <div>
                      <p className="font-semibold">{log.stage}</p>
                      <p className="text-slate-400 text-xs">{log.description}</p>
                    </div>
                    <span className="text-xs text-slate-400">{log.at ? new Date(log.at).toLocaleTimeString() : '—'}</span>
                  </div>
                  <p className="text-xs text-slate-400 mt-1">
                    Produto: <span className="text-white">{log.name}</span> ({log.productId})
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>
      </main>

      <Transition appear show={isModalOpen} as={Fragment}>
        <Dialog as="div" className="relative z-50" onClose={() => setIsModalOpen(false)}>
          <Transition.Child
            as={Fragment}
            enter="ease-out duration-200"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="ease-in duration-150"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="fixed inset-0 bg-black/60" />
          </Transition.Child>

          <div className="fixed inset-0 overflow-y-auto">
            <div className="flex min-h-full items-center justify-center p-4 text-center">
              <Transition.Child
                as={Fragment}
                enter="ease-out duration-200"
                enterFrom="opacity-0 scale-95"
                enterTo="opacity-100 scale-100"
                leave="ease-in duration-150"
                leaveFrom="opacity-100 scale-100"
                leaveTo="opacity-0 scale-95"
              >
                <Dialog.Panel className="w-full max-w-lg transform overflow-hidden rounded-2xl bg-bg-card p-6 text-left align-middle shadow-xl border border-white/10">
                  <Dialog.Title className="text-lg font-semibold">Criar Produto</Dialog.Title>
                  <p className="text-sm text-slate-400 mt-1">
                    Envia para a Command API (Postgres/Event Store) e dispara evento ProductCreated no Kafka.
                  </p>

                  <form
                    className="mt-4 space-y-3"
                    onSubmit={(e) => {
                      e.preventDefault()
                      createProduct.mutate({
                        name: form.name,
                        type: form.type,
                        price: Number(form.price),
                      })
                    }}
                  >
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <label className="text-sm text-slate-200 space-y-1">
                        Nome
                        <input
                          value={form.name}
                          onChange={(e) => setForm({ ...form, name: e.target.value })}
                          className="w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2"
                          required
                          placeholder="Teclado 75% RGB"
                        />
                      </label>
                      <label className="text-sm text-slate-200 space-y-1">
                        Tipo
                        <select
                          value={form.type}
                          onChange={(e) => setForm({ ...form, type: e.target.value })}
                          className="w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2"
                        >
                          {productTypes.map((type) => (
                            <option key={type}>{type}</option>
                          ))}
                        </select>
                      </label>
                    </div>

                    <label className="text-sm text-slate-200 space-y-1">
                      Preço (R$)
                      <input
                        type="number"
                        min="0"
                        step="0.01"
                        value={form.price}
                        onChange={(e) => setForm({ ...form, price: e.target.value })}
                        className="w-full rounded-xl bg-white/5 border border-white/10 px-3 py-2"
                        required
                        placeholder="799.90"
                      />
                    </label>

                    <div className="flex justify-end gap-2 pt-2">
                      <button
                        type="button"
                        onClick={() => setIsModalOpen(false)}
                        className="px-4 py-2 rounded-xl border border-white/10 text-sm"
                      >
                        Cancelar
                      </button>
                      <button
                        type="submit"
                        disabled={createProduct.isPending}
                        className="px-4 py-2 rounded-xl bg-gradient-to-r from-accent-blue to-accent-purple text-sm font-semibold shadow-neon disabled:opacity-50"
                      >
                        {createProduct.isPending ? 'Enviando...' : 'Salvar'}
                      </button>
                    </div>
                  </form>
                </Dialog.Panel>
              </Transition.Child>
            </div>
          </div>
        </Dialog>
      </Transition>

      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-6 right-6 px-4 py-3 rounded-xl bg-bg-card border border-accent-blue text-sm shadow-neon flex items-center gap-2"
          >
            {toast}
            <button onClick={() => setToast('')} className="text-slate-400 text-xs">
              fechar
            </button>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}

export default App
