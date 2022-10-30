import {Link} from "react-router-dom"
import {useEffect, useState} from "react"
import {useNavigate} from "react-router-dom"
import {API_URL} from "./utils"

function ClubRegister() {
    const INITIAL_CLUB = {
        name: "",
        members: []
    }
    const [club, setClub] = useState(INITIAL_CLUB)
    const [msgResponse, setMsgResponse] = useState()
    const [userCount, setUserCount] = useState(1)
    const navigate = useNavigate()

    useEffect(() => {
        if (msgResponse && msgResponse.status === 200) {
            navigate("/");
        }
    }, [msgResponse]);

    const postData = async (url = '', data = {}) => {
        const response = await fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        return response.json()
    }

    const handleSubmit = (event) => {
        event.preventDefault()
        postData(`${API_URL}/clubs`, club)
            .then((data) => {
                setMsgResponse(data)
            })
        setClub(INITIAL_CLUB)
        setUserCount(1)
    }

    const handleInput = (event) => {
        event.preventDefault()
        if (event.target.name === "club") {
            setClub(prev => ({
                ...prev,
                name: event.target.value
            }))
        } else {
            let newMembers = club.members
            newMembers[event.target.id] = event.target.value
            setClub(prev => ({
                ...prev,
                members: newMembers
            }))
        }
    }

    const renderUserInputs = () => {
        let inputs = []
        for (let i = 0; i < userCount; i++) {
            inputs.push(<input key={`user-${i}`} id={i} name={`user-${i}`}
                               value={club.members[`${i}`] || ''} onChange={handleInput} />)
        }

        return inputs
    }

    const addNewUserInput = (event) => {
        event.preventDefault()
        setUserCount(userCount + 1)
    }

    return <div>
        <h1>Club register</h1>
        <form onSubmit={handleSubmit}>
            <label>Club name</label>
            <input name="club" value={club.name} onChange={handleInput} />
            <br />
            <label>User names</label>
            <button onClick={addNewUserInput}>Add user</button>
            <br />
            {renderUserInputs()}
            <br />
            <button onSubmit={handleSubmit}>Submit</button>
        </form>
        <div style={{ color: "red" }}>{msgResponse && msgResponse.status !== 200 && msgResponse.message}</div>
        <Link to={`/`}><span>Return main page</span></Link>
    </div>
}

export default ClubRegister