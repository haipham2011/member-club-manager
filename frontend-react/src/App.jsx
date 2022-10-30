import {useEffect, useState} from 'react'
import { Link } from "react-router-dom";
import {API_URL} from "./utils";

function App() {
  const [clubs, setClubs] = useState(null)

  useEffect(() => {
      fetch(`${API_URL}/clubs`).then((response) => response.json())
          .then((data) => {
            setClubs(data.clubs)
          });
  }, [])

  return (
    clubs && <div>
      {clubs.map(club => (<div key={club.id}>
        <h2>{club.name}</h2>
        <p>Id: {club.id}</p>
        <p>Shortcut: {club.shortcut}</p>
        <p>Members: {club.members.map(mem => mem.name).reduce((a, b) => a + ", " + b)}</p>
      </div>))}
        <Link to={`register`}><span>Register new club</span></Link>
    </div>
  )
}

export default App
