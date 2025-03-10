// const useApi = (baseUrl: string) => {
//   return {
//     async createUser(
//       nombre: string,
//       username: string,
//       password: string,
//       rol: string
//     ) {
//       const response = await fetch(`${baseUrl}/admins/usuarios/`, {
//         method: "POST",
//         body: JSON.stringify({ nombre, username, password, rol }),
//         headers: {
//           "Content-Type": "application/json",
//         },
//       });

//       return response.json();
//     },

//     async getMedics() {
//       const response = await fetch(`${baseUrl}/admins/usuario`, {
//         method: "GET",
//         headers: {
//           "Content-Type": "application/json",
//         },
//       });

//       return response.json();
//     },

//     async createAppointment(
//       fecha: string,
//       hora: string,
//       motivo: string,
//       pciente_id: number,
//       medico_id: number
//     ) {
//       const response = await fetch(`${baseUrl}/pacientes/cita`, {
//         method: "POST",
//         body: JSON.stringify({
//           fecha,
//           hora,
//           motivo,
//           pciente_id,
//           medico_id,
//         }),
//         headers: {
//           "Content-Type": "application/json",
//         },
//       });

//       return response.json();
//     },

//     async getAppointments() {
//       const response = await fetch(`${baseUrl}/admins/cita`, {
//         method: "GET",
//         headers: {
//           "Content-Type": "application/json",
//         },
//       });

//       return response.json();
//     },
//   };
// };

// export default useApi;
