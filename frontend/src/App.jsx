import { useState, useEffect } from 'react';
import axios from 'axios';

const BACKEND_BASE_URL = 'http://localhost:8081';

function App() {

  // --- STATE ---
  const [patients, setPatients] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [polyclinics, setPolyclinics] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [filteredDoctors, setFilteredDoctors] = useState([]);

  const [editingPatientId, setEditingPatientId] = useState(null);
  const [editingAppointmentId, setEditingAppointmentId] = useState(null);

  const [loading, setLoading] = useState(false);
  const [submittingOpenEmr, setSubmittingOpenEmr] = useState(false);
  const [toast, setToast] = useState(null);

  const [activeTab, setActiveTab] = useState('patients');

  // Search states
  const [patientSearch, setPatientSearch] = useState('');
  const [appointmentSearch, setAppointmentSearch] = useState('');

  const [patientForm, setPatientForm] = useState({
    firstName: '', lastName: '', gender: 'M', dateOfBirth: '', phoneNumber: '', address: ''
  });

  const [appointmentForm, setAppointmentForm] = useState({
    patientId: '', doctorId: '', polyclinicId: '', appointmentDate: '', reason: ''
  });

  // --- TOAST ---
  const showToast = (msg, type = "success") => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 2500);
  };

  // --- FETCH ---
  const fetchData = async () => {
    setLoading(true);
    try {
      const [p, a, pol, doc] = await Promise.all([
        axios.get(`${BACKEND_BASE_URL}/api/patients`),
        axios.get(`${BACKEND_BASE_URL}/api/appointments`),
        axios.get(`${BACKEND_BASE_URL}/api/polyclinics`),
        axios.get(`${BACKEND_BASE_URL}/api/doctors`),
      ]);
      setPatients(p.data);
      setAppointments(a.data);
      setPolyclinics(pol.data);
      setDoctors(doc.data);
      setFilteredDoctors(doc.data);
    } catch (err) {
      showToast("Veri çekilemedi", "error");
    }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  // --- SEARCH ---
  const displayedPatients = patientSearch.trim()
    ? patients.filter(p =>
      `${p.firstName} ${p.lastName} ${p.phoneNumber || ''}`.toLowerCase().includes(patientSearch.toLowerCase())
    )
    : patients;

  // NOTE: helpers (getPatientName etc.) are not yet defined here so we inline the lookups
  const displayedAppointments = appointmentSearch.trim()
    ? appointments.filter(a => {
      const pat = patients.find(x => x.id === a.patientId);
      const doc = doctors.find(x => x.id === a.doctorId);
      const combined = [
        pat ? `${pat.firstName} ${pat.lastName}` : '',
        doc ? `${doc.title} ${doc.firstName} ${doc.lastName}` : '',
        a.reason || ''
      ].join(' ').toLowerCase();
      return combined.includes(appointmentSearch.toLowerCase());
    })
    : appointments;

  // --- POLYCLINIC CHANGE → FILTER DOCTORS ---
  const handlePolyclinicChange = (polyclinicId) => {
    setAppointmentForm(prev => ({ ...prev, polyclinicId, doctorId: '' }));
    if (polyclinicId) {
      setFilteredDoctors(doctors.filter(d => String(d.polyclinicId) === String(polyclinicId)));
    } else {
      setFilteredDoctors(doctors);
    }
  };

  // --- PATIENT ---
  const handlePatientSubmit = async (e) => {
    e.preventDefault();
    const patientPayload = {
      firstName: patientForm.firstName,
      lastName: patientForm.lastName,
      dateOfBirth: patientForm.dateOfBirth,
      phoneNumber: patientForm.phoneNumber,
      address: patientForm.address
    };

    try {
      if (editingPatientId) {
        await axios.put(`${BACKEND_BASE_URL}/api/patients/${editingPatientId}`, patientPayload);
        showToast("Hasta güncellendi");
      } else {
        await axios.post(`${BACKEND_BASE_URL}/api/patients`, patientPayload);
        showToast("Hasta eklendi");
      }
      setEditingPatientId(null);
      setPatientForm({ firstName: '', lastName: '', gender: 'M', dateOfBirth: '', phoneNumber: '', address: '' });
      fetchData();
    } catch {
      showToast("Hasta işlemi başarısız", "error");
    }
  };

  const handleDeletePatient = async (id) => {
    if (!window.confirm("Silmek istiyor musun?")) return;
    await axios.delete(`${BACKEND_BASE_URL}/api/patients/${id}`);
    showToast("Hasta silindi");
    fetchData();
  };

  const handleEditClick = (p) => {
    setEditingPatientId(p.id);
    setPatientForm({ ...p, gender: p.gender || 'M' });
    setActiveTab('patients');
  };

  // --- APPOINTMENT ---
  const handleAppointmentSubmit = async (e) => {
    e.preventDefault();

    const splitDateTime = (dateTimeLocal) => {
      if (!dateTimeLocal) return null;
      const date = dateTimeLocal.slice(0, 10);
      const startTime = dateTimeLocal.slice(11, 16);
      if (!date || !startTime) return null;
      const startDate = new Date(dateTimeLocal);
      if (Number.isNaN(startDate.getTime())) return null;
      const endDate = new Date(startDate.getTime() + 30 * 60 * 1000);
      const hh = String(endDate.getHours()).padStart(2, '0');
      const mm = String(endDate.getMinutes()).padStart(2, '0');
      return { date, startTime, endTime: `${hh}:${mm}` };
    };

    try {
      if (editingAppointmentId) {
        const payload = {
          patientId: Number(appointmentForm.patientId),
          doctorId: appointmentForm.doctorId ? Number(appointmentForm.doctorId) : null,
          appointmentDate: appointmentForm.appointmentDate,
          reason: appointmentForm.reason
        };
        await axios.put(`${BACKEND_BASE_URL}/api/appointments/${editingAppointmentId}`, payload);
        showToast("Randevu güncellendi");
      } else {
        const schedule = splitDateTime(appointmentForm.appointmentDate);
        if (!schedule) {
          showToast("Randevu tarihi/saati geçersiz", "error");
          return;
        }

        const selectedPatient = patients.find(p => String(p.id) === String(appointmentForm.patientId));
        const resolvedFirstName = (selectedPatient?.firstName || patientForm.firstName || '').trim();
        const resolvedLastName = (selectedPatient?.lastName || patientForm.lastName || '').trim();
        const resolvedDob = (selectedPatient?.dateOfBirth || patientForm.dateOfBirth || '').trim();

        if (!resolvedFirstName || !resolvedLastName || !resolvedDob) {
          showToast("OpenEMR için hasta seçin veya ad/soyad/doğum tarihi girin", "error");
          return;
        }

        const payload = {
          fname: resolvedFirstName,
          lname: resolvedLastName,
          sex: patientForm.gender || 'M',
          dob: resolvedDob,
          date: schedule.date,
          startTime: schedule.startTime,
          endTime: schedule.endTime
        };

        setSubmittingOpenEmr(true);
        const response = await axios.post(`${BACKEND_BASE_URL}/api/patient-with-appointment`, payload);

        let localPatientId = selectedPatient?.id;
        if (!localPatientId) {
          const localPatientPayload = {
            firstName: resolvedFirstName,
            lastName: resolvedLastName,
            dateOfBirth: resolvedDob,
            phoneNumber: patientForm.phoneNumber || '',
            address: patientForm.address || ''
          };
          const localPatientResponse = await axios.post(`${BACKEND_BASE_URL}/api/patients`, localPatientPayload);
          localPatientId = localPatientResponse?.data?.id;
        }

        if (localPatientId) {
          const localAppointmentPayload = {
            patientId: Number(localPatientId),
            doctorId: appointmentForm.doctorId ? Number(appointmentForm.doctorId) : null,
            appointmentDate: appointmentForm.appointmentDate,
            reason: appointmentForm.reason || 'Checkup'
          };
          await axios.post(`${BACKEND_BASE_URL}/api/appointments`, localAppointmentPayload);
        }

        showToast(`OpenEMR kaydı oluşturuldu. PID: ${response.data.patientId}`);
      }

      setEditingAppointmentId(null);
      setAppointmentForm({ patientId: '', doctorId: '', polyclinicId: '', appointmentDate: '', reason: '' });
      setFilteredDoctors(doctors);
      fetchData();

    } catch (err) {
      const backendMessage = err?.response?.data?.message;
      showToast(backendMessage || "Randevu işlemi başarısız", "error");
    } finally {
      setSubmittingOpenEmr(false);
    }
  };

  const handleEditApptClick = (a) => {
    setEditingAppointmentId(a.id);
    const doc = doctors.find(d => d.id === a.doctorId);
    const polyId = doc ? String(doc.polyclinicId) : '';
    setAppointmentForm({
      patientId: a.patientId,
      doctorId: a.doctorId || '',
      polyclinicId: polyId,
      appointmentDate: a.appointmentDate,
      reason: a.reason
    });
    if (polyId) {
      setFilteredDoctors(doctors.filter(d => String(d.polyclinicId) === polyId));
    }
    setActiveTab('appointments');
  };

  const handleDeleteAppointment = async (id) => {
    if (!window.confirm("Randevu silinsin mi?")) return;
    await axios.delete(`${BACKEND_BASE_URL}/api/appointments/${id}`);
    showToast("Randevu silindi");
    fetchData();
  };

  // --- HELPERS ---
  const getPatientName = (id) => {
    const p = patients.find(x => x.id === id);
    return p ? `${p.firstName} ${p.lastName}` : 'Bilinmeyen';
  };

  const getDoctorName = (id) => {
    if (!id) return '-';
    const d = doctors.find(x => x.id === id);
    return d ? `${d.title} ${d.firstName} ${d.lastName}` : 'Bilinmeyen';
  };

  const getPolyclinicName = (doctorId) => {
    if (!doctorId) return '-';
    const d = doctors.find(x => x.id === doctorId);
    if (!d) return '-';
    const p = polyclinics.find(x => x.id === d.polyclinicId);
    return p ? p.name : '-';
  };

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '20px', fontFamily: 'Inter, sans-serif' }}>

      <h1 style={{ textAlign: 'center', color: '#8888ffff', marginBottom: '5px' }}>🏥 Mini Hasta Yönetim Sistemi</h1>
      <p style={{ textAlign: 'center', color: '#888', marginTop: 0, marginBottom: '20px', fontSize: '14px' }}>
        {polyclinics.length} Poliklinik · {doctors.length} Doktor · {patients.length} Hasta · {appointments.length} Randevu
      </p>

      {/* TOAST */}
      {toast && (
        <div style={{
          position: 'fixed', top: 20, right: 20, zIndex: 999,
          background: toast.type === "error" ? "#e74c3c" : "#2ecc71",
          color: 'white', padding: '12px 24px', borderRadius: '8px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)', fontSize: '14px'
        }}>
          {toast.msg}
        </div>
      )}

      {/* LOADING */}
      {loading && <p style={{ textAlign: 'center' }}>Yükleniyor...</p>}

      {/* TABS */}
      <div style={{ display: 'flex', gap: '0', marginBottom: '20px', borderBottom: '2px solid #e0e0e0' }}>
        {[
          { key: 'patients', label: '🧑‍⚕️ Hastalar' },
          { key: 'appointments', label: '📅 Randevular' },
          { key: 'directory', label: '🏥 Poliklinik & Doktorlar' },
        ].map(tab => (
          <button key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            style={{
              padding: '10px 24px', border: 'none', cursor: 'pointer',
              background: activeTab === tab.key ? '#3498db' : 'transparent',
              color: activeTab === tab.key ? 'white' : '#555',
              fontWeight: activeTab === tab.key ? 'bold' : 'normal',
              borderRadius: '8px 8px 0 0', fontSize: '14px',
              transition: 'all 0.2s'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* ═══════════ TAB: HASTALAR ═══════════ */}
      {activeTab === 'patients' && (
        <div style={{ display: 'flex', gap: '20px' }}>
          {/* FORM */}
          <div style={{ flex: '0 0 320px', background: '#f8f9fa', padding: '20px', borderRadius: '12px' }}>
            <h3 style={{ marginTop: 0 }}>{editingPatientId ? '✏️ Hasta Düzenle' : '➕ Yeni Hasta'}</h3>
            <form onSubmit={handlePatientSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <input placeholder="Ad" value={patientForm.firstName}
                onChange={e => setPatientForm({ ...patientForm, firstName: e.target.value })} style={inputStyle} />
              <input placeholder="Soyad" value={patientForm.lastName}
                onChange={e => setPatientForm({ ...patientForm, lastName: e.target.value })} style={inputStyle} />
              <input type="date" value={patientForm.dateOfBirth}
                onChange={e => setPatientForm({ ...patientForm, dateOfBirth: e.target.value })} style={inputStyle} />
              <input placeholder="Telefon" value={patientForm.phoneNumber}
                onChange={e => setPatientForm({ ...patientForm, phoneNumber: e.target.value })} style={inputStyle} />
              <textarea placeholder="Adres" value={patientForm.address}
                onChange={e => setPatientForm({ ...patientForm, address: e.target.value })} style={inputStyle} />
              <button style={{ ...btnStyle, background: '#3498db' }}>
                {editingPatientId ? "Güncelle" : "Kaydet"}
              </button>
              {editingPatientId && (
                <button type="button" onClick={() => {
                  setEditingPatientId(null);
                  setPatientForm({ firstName: '', lastName: '', gender: 'M', dateOfBirth: '', phoneNumber: '', address: '' });
                }} style={{ ...btnStyle, background: '#95a5a6' }}>
                  İptal
                </button>
              )}
            </form>
          </div>

          {/* LIST */}
          <div style={{ flex: 1 }}>
            <input placeholder="🔍 Hasta ara (ad, soyad, telefon)..."
              value={patientSearch}
              onChange={e => setPatientSearch(e.target.value)}
              style={{ ...inputStyle, marginBottom: '12px', width: '100%', boxSizing: 'border-box' }}
            />
            {displayedPatients.length === 0 ? (
              <p style={{ color: '#999' }}>Kayıtlı hasta yok</p>
            ) : (
              displayedPatients.map(p => (
                <div key={p.id} style={cardStyle}>
                  <div>
                    <strong>{p.firstName} {p.lastName}</strong>
                    <div style={{ fontSize: '12px', color: '#888' }}>
                      📞 {p.phoneNumber || '-'} · 🎂 {p.dateOfBirth || '-'}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '5px' }}>
                    <button onClick={() => handleEditClick(p)} style={iconBtn}>✏️</button>
                    <button onClick={() => handleDeletePatient(p.id)} style={iconBtn}>🗑️</button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* ═══════════ TAB: RANDEVULAR ═══════════ */}
      {activeTab === 'appointments' && (
        <div style={{ display: 'flex', gap: '20px' }}>
          {/* FORM */}
          <div style={{ flex: '0 0 320px', background: '#eef2f5', padding: '20px', borderRadius: '12px' }}>
            <h3 style={{ marginTop: 0 }}>{editingAppointmentId ? '✏️ Randevu Düzenle' : '➕ Yeni Randevu'}</h3>
            <form onSubmit={handleAppointmentSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>

              <select value={appointmentForm.patientId}
                onChange={e => setAppointmentForm({ ...appointmentForm, patientId: e.target.value })}
                style={inputStyle}>
                <option value="">Hasta seç</option>
                {patients.map(p => <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>)}
              </select>

              <select value={appointmentForm.polyclinicId}
                onChange={e => handlePolyclinicChange(e.target.value)}
                style={inputStyle}>
                <option value="">Poliklinik seç</option>
                {polyclinics.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>

              <select value={appointmentForm.doctorId}
                onChange={e => setAppointmentForm({ ...appointmentForm, doctorId: e.target.value })}
                style={inputStyle}>
                <option value="">Doktor seç</option>
                {filteredDoctors.map(d => (
                  <option key={d.id} value={d.id}>{d.title} {d.firstName} {d.lastName}</option>
                ))}
              </select>

              <input type="datetime-local"
                value={appointmentForm.appointmentDate}
                onChange={e => setAppointmentForm({ ...appointmentForm, appointmentDate: e.target.value })}
                style={inputStyle}
              />

              <input placeholder="Sebep"
                value={appointmentForm.reason}
                onChange={e => setAppointmentForm({ ...appointmentForm, reason: e.target.value })}
                style={inputStyle}
              />

              <button style={{ ...btnStyle, background: '#2ecc71' }} disabled={submittingOpenEmr}>
                {submittingOpenEmr ? "Kaydediliyor..." : (editingAppointmentId ? "Güncelle" : "Oluştur")}
              </button>
              {editingAppointmentId && (
                <button type="button" onClick={() => {
                  setEditingAppointmentId(null);
                  setAppointmentForm({ patientId: '', doctorId: '', polyclinicId: '', appointmentDate: '', reason: '' });
                  setFilteredDoctors(doctors);
                }} style={{ ...btnStyle, background: '#95a5a6' }}>
                  İptal
                </button>
              )}
            </form>
          </div>

          {/* LIST */}
          <div style={{ flex: 1 }}>
            <input placeholder="🔍 Randevu ara (hasta, doktor, sebep)..."
              value={appointmentSearch}
              onChange={e => setAppointmentSearch(e.target.value)}
              style={{ ...inputStyle, marginBottom: '12px', width: '100%', boxSizing: 'border-box' }}
            />
            {displayedAppointments.length === 0 ? (
              <p style={{ color: '#999' }}>Randevu yok</p>
            ) : (
              displayedAppointments.map(a => (
                <div key={a.id} style={{ ...cardStyle, borderLeft: '4px solid #2ecc71' }}>
                  <div>
                    <strong>{getPatientName(a.patientId)}</strong>
                    <div style={{ fontSize: '12px', color: '#555' }}>
                      🩺 {getDoctorName(a.doctorId)} · 🏥 {getPolyclinicName(a.doctorId)}
                    </div>
                    <div style={{ fontSize: '12px', color: '#888' }}>
                      📋 {a.reason || '-'} · 📅 {a.appointmentDate ? a.appointmentDate.replace('T', ' ') : '-'}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
                    <button onClick={() => handleEditApptClick(a)} style={iconBtn}>✏️</button>
                    <button onClick={() => handleDeleteAppointment(a.id)} style={iconBtn}>🗑️</button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* ═══════════ TAB: POLİKLİNİK & DOKTORLAR ═══════════ */}
      {activeTab === 'directory' && (
        <div>
          {polyclinics.length === 0 ? (
            <p style={{ color: '#999' }}>Henüz poliklinik yok</p>
          ) : (
            polyclinics.map(poly => (
              <div key={poly.id} style={{ marginBottom: '20px' }}>
                <div style={{
                  background: '#2E5496', color: 'white', padding: '10px 16px',
                  borderRadius: '8px 8px 0 0', fontWeight: 'bold', fontSize: '15px'
                }}>
                  🏥 {poly.name}
                  <span style={{ fontWeight: 'normal', fontSize: '12px', marginLeft: '10px', opacity: 0.8 }}>
                    {poly.description}
                  </span>
                </div>
                <div style={{
                  background: '#f8f9fa', padding: '10px 16px',
                  borderRadius: '0 0 8px 8px', border: '1px solid #e0e0e0', borderTop: 'none'
                }}>
                  {doctors.filter(d => d.polyclinicId === poly.id).length === 0 ? (
                    <span style={{ color: '#999', fontSize: '13px' }}>Bu poliklinikte doktor yok</span>
                  ) : (
                    doctors.filter(d => d.polyclinicId === poly.id).map(d => (
                      <div key={d.id} style={{
                        display: 'inline-block', background: 'white', border: '1px solid #ddd',
                        borderRadius: '6px', padding: '6px 12px', margin: '4px', fontSize: '13px'
                      }}>
                        🩺 <strong>{d.title}</strong> {d.firstName} {d.lastName}
                      </div>
                    ))
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

    </div>
  );
}

const inputStyle = { padding: '10px', border: '1px solid #ddd', borderRadius: '6px', fontSize: '14px', outline: 'none' };
const btnStyle = { padding: '10px', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold' };
const iconBtn = { background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px', padding: '4px' };
const cardStyle = {
  background: 'white',
  padding: '12px 16px',
  borderRadius: '8px',
  border: '1px solid #e0e0e0',
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '8px',
  transition: 'box-shadow 0.2s'
};

export default App;