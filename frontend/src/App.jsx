import { useState, useEffect } from 'react';
import axios from 'axios';

function App() {

  // --- STATE ---
  const [patients, setPatients] = useState([]);
  const [appointments, setAppointments] = useState([]);

  const [editingPatientId, setEditingPatientId] = useState(null);
  const [editingAppointmentId, setEditingAppointmentId] = useState(null);

  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const [patientForm, setPatientForm] = useState({
    firstName: '', lastName: '', dateOfBirth: '', phoneNumber: '', address: ''
  });

  const [appointmentForm, setAppointmentForm] = useState({
    patientId: '', appointmentDate: '', reason: ''
  });

  // --- TOAST ---
  const showToast = (msg, type="success") => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 2500);
  };

  // --- FETCH ---
  const fetchData = async () => {
    setLoading(true);
    try {
      const p = await axios.get('http://localhost:8080/api/patients');
      const a = await axios.get('http://localhost:8080/api/appointments');
      setPatients(p.data);
      setAppointments(a.data);
    } catch (err) {
      showToast("Veri çekilemedi", "error");
    }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  // --- PATIENT ---
  const handlePatientSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingPatientId) {
        await axios.put(`http://localhost:8080/api/patients/${editingPatientId}`, patientForm);
        showToast("Hasta güncellendi");
      } else {
        await axios.post(`http://localhost:8080/api/patients`, patientForm);
        showToast("Hasta eklendi");
      }
      setEditingPatientId(null);
      setPatientForm({ firstName:'', lastName:'', dateOfBirth:'', phoneNumber:'', address:'' });
      fetchData();
    } catch {
      showToast("Hasta işlemi başarısız", "error");
    }
  };

  const handleDeletePatient = async (id) => {
    if (!window.confirm("Silmek istiyor musun?")) return;
    await axios.delete(`http://localhost:8080/api/patients/${id}`);
    showToast("Hasta silindi");
    fetchData();
  };

  const handleEditClick = (p) => {
    setEditingPatientId(p.id);
    setPatientForm(p);
  };

  // --- APPOINTMENT ---
  const handleAppointmentSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      patientId: Number(appointmentForm.patientId),
      appointmentDate: appointmentForm.appointmentDate,
      reason: appointmentForm.reason
    };

    try {
      if (editingAppointmentId) {
        await axios.put(`http://localhost:8080/api/appointments/${editingAppointmentId}`, payload);
        showToast("Randevu güncellendi");
      } else {
        await axios.post(`http://localhost:8080/api/appointments`, payload);
        showToast("Randevu oluşturuldu");
      }

      setEditingAppointmentId(null);
      setAppointmentForm({ patientId:'', appointmentDate:'', reason:'' });
      fetchData();

    } catch {
      showToast("Randevu işlemi başarısız", "error");
    }
  };

  const handleEditApptClick = (a) => {
    setEditingAppointmentId(a.id);
    setAppointmentForm({
      patientId: a.patientId,
      appointmentDate: a.appointmentDate,
      reason: a.reason
    });
  };

  const handleDeleteAppointment = async (id) => {
    if (!window.confirm("Randevu silinsin mi?")) return;
    await axios.delete(`http://localhost:8080/api/appointments/${id}`);
    showToast("Randevu silindi");
    fetchData();
  };

  const getPatientName = (id) => {
    const p = patients.find(x => x.id === id);
    return p ? `${p.firstName} ${p.lastName}` : 'Bilinmeyen';
  };

  return (
    <div style={{ maxWidth:'1000px', margin:'0 auto', padding:'20px', fontFamily:'sans-serif' }}>

      <h1 style={{ textAlign:'center' }}> Mini Hasta Yönetim Sistemi</h1>

      {/* TOAST */}
      {toast && (
        <div style={{
          position:'fixed',
          top:20,
          right:20,
          background: toast.type === "error" ? "#e74c3c" : "#2ecc71",
          color:'white',
          padding:'10px 20px',
          borderRadius:'6px'
        }}>
          {toast.msg}
        </div>
      )}

      {/* LOADING */}
      {loading && <p style={{ textAlign:'center' }}>Yükleniyor...</p>}

      <div style={{ display:'flex', gap:'20px', marginTop:'30px' }}>

        {/* SOL PANEL */}
        <div style={{ flex:1, background:'#f8f9fa', padding:'20px', borderRadius:'8px' }}>
          <h3>🧑‍⚕️ Hasta</h3>

          <form onSubmit={handlePatientSubmit} style={{ display:'flex', flexDirection:'column', gap:'10px' }}>
            <input placeholder="Ad" value={patientForm.firstName} onChange={e=>setPatientForm({...patientForm, firstName:e.target.value})} style={inputStyle}/>
            <input placeholder="Soyad" value={patientForm.lastName} onChange={e=>setPatientForm({...patientForm, lastName:e.target.value})} style={inputStyle}/>
            <input type="date" value={patientForm.dateOfBirth} onChange={e=>setPatientForm({...patientForm, dateOfBirth:e.target.value})} style={inputStyle}/>
            <input placeholder="Telefon" value={patientForm.phoneNumber} onChange={e=>setPatientForm({...patientForm, phoneNumber:e.target.value})} style={inputStyle}/>
            <textarea placeholder="Adres" value={patientForm.address} onChange={e=>setPatientForm({...patientForm, address:e.target.value})} style={inputStyle}/>

            <button style={{...btnStyle, background:'#3498db'}}>
              {editingPatientId ? "Güncelle" : "Kaydet"}
            </button>
          </form>

          {patients.length === 0 ? (
            <p>Kayıtlı hasta yok</p>
          ) : (
            patients.map(p => (
              <div key={p.id} style={cardStyle}>
                <div>{p.firstName} {p.lastName}</div>
                <div>
                  <button onClick={()=>handleEditClick(p)}>✏️</button>
                  <button onClick={()=>handleDeletePatient(p.id)}>🗑️</button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* SAĞ PANEL */}
        <div style={{ flex:1, background:'#eef2f5', padding:'20px', borderRadius:'8px' }}>
          <h3>📅 Randevu</h3>

          <form onSubmit={handleAppointmentSubmit} style={{ display:'flex', flexDirection:'column', gap:'10px' }}>
            <select value={appointmentForm.patientId}
              onChange={e=>setAppointmentForm({...appointmentForm, patientId:e.target.value})}
              style={inputStyle}>
              <option value="">Hasta seç</option>
              {patients.map(p => <option key={p.id} value={p.id}>{p.firstName}</option>)}
            </select>

            <input type="datetime-local"
              value={appointmentForm.appointmentDate}
              onChange={e=>setAppointmentForm({...appointmentForm, appointmentDate:e.target.value})}
              style={inputStyle}
            />

            <input placeholder="Sebep"
              value={appointmentForm.reason}
              onChange={e=>setAppointmentForm({...appointmentForm, reason:e.target.value})}
              style={inputStyle}
            />

            <button style={{...btnStyle, background:'#2ecc71'}}>
              {editingAppointmentId ? "Güncelle" : "Oluştur"}
            </button>
          </form>

          {appointments.length === 0 ? (
            <p>Randevu yok</p>
          ) : (
            appointments.map(a => (
              <div key={a.id} style={{...cardStyle, borderLeft:'4px solid #2ecc71'}}>
                <strong>{getPatientName(a.patientId)}</strong>
                <div>{a.reason}</div>

                <div style={{ display:'flex', gap:'5px' }}>
                  <button onClick={()=>handleEditApptClick(a)}>✏️</button>
                  <button onClick={()=>handleDeleteAppointment(a.id)}>🗑️</button>
                </div>
              </div>
            ))
          )}
        </div>

      </div>
    </div>
  );
}

const inputStyle = { padding:'8px', border:'1px solid #ccc', borderRadius:'4px' };
const btnStyle = { padding:'10px', color:'white', border:'none', borderRadius:'4px' };
const cardStyle = {
  background:'white',
  padding:'10px',
  borderRadius:'6px',
  border:'1px solid #ddd',
  display:'flex',
  justifyContent:'space-between',
  marginTop:'10px'
};

export default App;