import { useState, useEffect } from 'react'
import './App.css'

const API_COMMAND = 'http://localhost:8080'
const API_QUERY = 'http://localhost:8081'

function App() {
  const [orders, setOrders] = useState([])
  const [trackingId, setTrackingId] = useState('')
  const [trackedOrder, setTrackedOrder] = useState(null)
  const [loading, setLoading] = useState(false)
  const [newOrder, setNewOrder] = useState({
    userId: '',
    totalAmount: '',
    items: ''
  })

  // Polling para atualizar status
  useEffect(() => {
    if (trackingId) {
      const interval = setInterval(async () => {
        try {
          const res = await fetch(`${API_QUERY}/orders/${trackingId}`)
          if (res.ok) {
            const data = await res.json()
            setTrackedOrder(data)
          }
        } catch (err) {
          console.error('Polling error:', err)
        }
      }, 2000)
      return () => clearInterval(interval)
    }
  }, [trackingId])

  const handleCreateOrder = async (e) => {
    e.preventDefault()
    setLoading(true)
    
    try {
      const res = await fetch(`${API_COMMAND}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: newOrder.userId,
          totalAmount: parseFloat(newOrder.totalAmount),
          items: newOrder.items.split(',').map(i => i.trim())
        })
      })
      
      const data = await res.json()
      setTrackingId(data.orderId)
      setOrders(prev => [{ id: data.orderId, status: 'PROCESSING' }, ...prev])
      setNewOrder({ userId: '', totalAmount: '', items: '' })
    } catch (err) {
      alert('Erro ao criar pedido: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status) => {
    const colors = {
      'CREATED': 'var(--accent-blue)',
      'PROCESSING': 'var(--accent-orange)',
      'STOCK_RESERVED': 'var(--accent-purple)',
      'COMPLETED': 'var(--accent-green)',
    }
    return colors[status] || 'var(--text-secondary)'
  }

  const getStatusEmoji = (status) => {
    const emojis = {
      'CREATED': '📝',
      'PROCESSING': '⏳',
      'STOCK_RESERVED': '📦',
      'COMPLETED': '✅',
    }
    return emojis[status] || '❓'
  }

  return (
    <div className="app">
      <header className="header">
        <div className="logo">
          <span className="logo-icon">🛒</span>
          <h1>Event-Driven Mall</h1>
        </div>
        <p className="subtitle">CQRS + Event Sourcing Demo</p>
      </header>

      <main className="main">
        {/* Criar Pedido */}
        <section className="card create-order">
          <h2>📤 Criar Novo Pedido</h2>
          <p className="card-desc">POST → Command API → Kafka → Workers</p>
          
          <form onSubmit={handleCreateOrder}>
            <div className="form-group">
              <label>User ID</label>
              <input
                type="text"
                value={newOrder.userId}
                onChange={e => setNewOrder({ ...newOrder, userId: e.target.value })}
                placeholder="usuario_123"
                required
              />
            </div>
            
            <div className="form-group">
              <label>Valor Total (R$)</label>
              <input
                type="number"
                step="0.01"
                value={newOrder.totalAmount}
                onChange={e => setNewOrder({ ...newOrder, totalAmount: e.target.value })}
                placeholder="199.90"
                required
              />
            </div>
            
            <div className="form-group">
              <label>Itens (separados por vírgula)</label>
              <input
                type="text"
                value={newOrder.items}
                onChange={e => setNewOrder({ ...newOrder, items: e.target.value })}
                placeholder="Teclado, Mouse, Monitor"
                required
              />
            </div>
            
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? '⏳ Enviando...' : '🚀 Criar Pedido'}
            </button>
          </form>
        </section>

        {/* Tracking */}
        {trackingId && (
          <section className="card tracking">
            <h2>🔍 Acompanhando Pedido</h2>
            <p className="order-id mono">{trackingId}</p>
            
            {trackedOrder ? (
              <div className="order-details">
                <div className="status-badge" style={{ borderColor: getStatusColor(trackedOrder.status) }}>
                  <span className="emoji">{getStatusEmoji(trackedOrder.status)}</span>
                  <span className="status-text" style={{ color: getStatusColor(trackedOrder.status) }}>
                    {trackedOrder.status}
                  </span>
                </div>

                <div className="timeline">
                  <h3>📜 Histórico de Eventos</h3>
                  {trackedOrder.history?.map((h, i) => (
                    <div key={i} className="timeline-item">
                      <span className="emoji">{getStatusEmoji(h.status)}</span>
                      <span className="status">{h.status}</span>
                      <span className="time mono">{new Date(h.at).toLocaleTimeString()}</span>
                    </div>
                  ))}
                </div>

                <div className="order-info">
                  <div><strong>Usuário:</strong> {trackedOrder.userId}</div>
                  <div><strong>Valor:</strong> R$ {trackedOrder.amount}</div>
                  <div><strong>Itens:</strong> {trackedOrder.items?.join(', ')}</div>
                </div>
              </div>
            ) : (
              <p className="polling">⏳ Aguardando projeção no MongoDB...</p>
            )}
          </section>
        )}

        {/* Arquitetura */}
        <section className="card architecture">
          <h2>⚡ Fluxo da Arquitetura</h2>
          <div className="flow">
            <div className="flow-item">
              <span className="service react">React</span>
              <span className="arrow">→</span>
              <span className="action">POST /orders</span>
            </div>
            <div className="flow-item">
              <span className="service java">Command API</span>
              <span className="arrow">→</span>
              <span className="action">Salva PostgreSQL + Kafka</span>
            </div>
            <div className="flow-item">
              <span className="service java">Inventory Worker</span>
              <span className="arrow">→</span>
              <span className="action">StockReserved</span>
            </div>
            <div className="flow-item">
              <span className="service clojure">Payment Worker</span>
              <span className="arrow">→</span>
              <span className="action">PaymentSuccess</span>
            </div>
            <div className="flow-item">
              <span className="service clojure">Projector</span>
              <span className="arrow">→</span>
              <span className="action">MongoDB (Read Model)</span>
            </div>
            <div className="flow-item">
              <span className="service java">Query API</span>
              <span className="arrow">→</span>
              <span className="action">GET /orders/{'{id}'}</span>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer">
        <p>Event-Driven Architecture • CQRS • Event Sourcing</p>
      </footer>
    </div>
  )
}

export default App

