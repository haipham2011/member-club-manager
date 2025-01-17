import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'
import ClubRegister from "./ClubRegister";
import {
    createBrowserRouter,
    RouterProvider
} from "react-router-dom";

const router = createBrowserRouter([
    {
        path: "/",
        element: <App />,
    },
    {
        path: "register",
        element: <ClubRegister />,
    }
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
      <RouterProvider router={router} />
  </React.StrictMode>
)
