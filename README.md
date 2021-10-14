MODULOS
1. Modulo general para deteccion de internet y mostrar el mensaje en toda la app
2. Actividad de informacion de sincronizacion
3. Modulo registrar y verificar:
    - cambios de anuncios
    - nuevas ayudas de voz
    - alerta con vibracion
    - Acceso automatico al Modulo de VERIFICACION Y REGISTRO
    - Configuraciones de las ayudas
    - CONSERVAR LA SELECCION DE "VERIFICAR Y REGISTRAR"
4. Subida de imagenes con modo offline unicamente para creacion de personal *Nuevo Desarrollo


PERFILACION

1.	Validacion de permisos para usuarios normales y administrativos en todos los modulos del aplicativo
2.	Para los permisos de creacion de: empleadores, proyectos, contratos y personal, en los usuarios normales, se agrego la opcion de seleccionar la empresa administradora.
3.	Validacion de permisos visualizacion con la respuesta de la API 401(UNAUTHORIZED), para el renderizado de contenido en todos los modulos. 
4.	optimizacion del manejo de datos en el modulo de contratos y proyectos

BUGS RESUELTOS

1. App M.Permisos No esta funcionado el permiso de crear proyectos en Administradora
2. App M.Permisos El permiso de actualizar administradora no funciona
3. App M.Sucursales La sucursal se sobrepone al logo de la empresa admon
4. App M.Administradora Permiso de modificar exclusivo de superuser
5. App M.Perfiles  El perfil de ADMINISTRADOR tiene permiso de DASHBOARD pero en el APP no se ve.
6. App M.Escaneo Se escanea un carnet de una pesona X y el app muestra otra persona Y
7. App M.Empresas Se reinicia el app cuando se intenta editar una emprsa
8. App M.Empresas : carga incorrecta del logo de la empresa
9. App M.Registrar y Verificar: Si esta funcionalidad no se esta usuando, el sistema debe cerrarla e ir a Inicio
10. APP M.Permisos Si se apagan estos permisos el APP no muestra el contrato
11. App M.Proyectos Diferencias entre App y Web
12. App M.Perfilaciones en proyectos. Corregir permisos de empresas asociadas y vinculadas
13. App M.Perfiles Cuando se apaga el permiso no ocultar el titulo
14. App M.Escaneo Contador del escaneo reducir a 30 segundos
15. App M.Administradora No se ven las sucursales de la administradora
16. cambio url para lista contratos en personal
17. inclusion de funcion para editar cargo y vigencia de un contrato desde el modulo de personal
18. revision de permisos de .view, y filtrado de listas segun administradora en usuarios normales 
19. modificación para el correcto escaneo de datos en el modulo de registrar y verificar para usuarios normales
20. Nuevo campo para AFP para el formulario de creacion de empleadores
21. Cambio de metodo de busqueda para la seleccion de contratantes y contratistas en el modulo de creacion de contratos
22. correccion de bug: App M.Contratos En el microservicio de Usuario Vinculados, las tarjetas de los usuarios no estan mostrando el rol
23. seleccion automatica de la empresa administradora cuando en los modulos que lo requieren, en caso que solo exista una.
24. solucion de bug: App M.Notificaciones Al hacer touch sobre la tarjeta de proyectos mostrada en notificaciones se reinica el app
25. correcion de bug con libreria de la camara: 
    App M.Personal No deja montar cierto tipo de imagenes
    App M.Personal Si se edita en la web la foto de una persona, no se ve reflejado en la app
26. modificacion en el modulo escanear y verificar, para que no sea posible enviar el reporte si no se selecciona una opcion valida
  .bug solucionado App M.Perfiles Usuario con permiso de crear contrato pero no lo puede hacer en el APP 
27. BUG 1810 -> fecha de sincronizacion en verificacion
28. Eliminacion de singletop en launch mode por generacion constante de errores
29. Lanzamiento correcto para habilitar el gps
30. Modificacion de estructura para abrir actividades como login, verificacion y main
31. bug solucionado App M.Perfiles Usuario con permiso de crear contrato pero no lo puede hacer en el APP
32. bug resuelto: App M.Detalle No se actualiza la ultime fecha de sincronizacion en DETALLE, cuando el usuario entra a REGISTRAR Y VERIFICAR 
33. App M.Registrarse Cambio de nombre de "Registrate" por "Crear Usuario"
34. Desarrollo: nueva opcion para habilitar/desabilitar botones del modulo de registrar y verificar
35. Desarrollo: App M.Empresa Falta campo de ARL en el formulario cuando se edita una empresa
36. Desarrollo: boton sincronizacion en el modulo de enrolamiento
37. Desarrollo: Visualizacion de terminos y condiciones en creacion de usuario
38. Desarrollo: Nuevos permisos en en el modulo de contratos y personal, relacionados con la vinculacion de personal a contratos
39. correccion bug: App M.Crear usuario No me funciona el link de Términos y condiciones