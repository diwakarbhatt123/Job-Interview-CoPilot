import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import Login from "@/pages/login";

const push = jest.fn();
const replace = jest.fn();

jest.mock("next/router", () => ({
  useRouter: () => ({
    push,
    replace,
    query: {},
  }),
}));

beforeEach(() => {
  push.mockReset();
  replace.mockReset();
  global.fetch = jest.fn();
});

it("shows error on invalid credentials", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: false,
    status: 401,
  });

  render(<Login />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/password/i), {
    target: {value: "wrong"},
  });

  fireEvent.click(screen.getByRole("button", {name: /login/i}));

  expect(await screen.findByText(/invalid email or password/i)).toBeInTheDocument();
});

it("redirects on success", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: true,
    status: 200,
  });

  render(<Login />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /login/i}));

  await waitFor(() => expect(replace).toHaveBeenCalledWith("/"));
});

it("shows generic error on server failure", async () => {
  (global.fetch as jest.Mock).mockResolvedValue({
    ok: false,
    status: 500,
  });

  render(<Login />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /login/i}));

  expect(
    await screen.findByText(/something went wrong/i)
  ).toBeInTheDocument();
});

it("shows network error on fetch failure", async () => {
  (global.fetch as jest.Mock).mockRejectedValue(new Error("Network error"));

  render(<Login />);

  fireEvent.change(screen.getByLabelText(/email/i), {
    target: {value: "user@example.com"},
  });
  fireEvent.change(screen.getByLabelText(/password/i), {
    target: {value: "secret"},
  });

  fireEvent.click(screen.getByRole("button", {name: /login/i}));

  expect(
    await screen.findByText(/network error/i)
  ).toBeInTheDocument();
});
